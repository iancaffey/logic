package io.logic;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import io.logic.Logic.Ignore;
import io.logic.Logic.Include;
import io.logic.Logic.Includes;
import io.logic.Logic.Mixin;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An annotation processor for {@link Logic}.
 * <p>
 * {@link LogicProcessor} generates both the <a href="https://github.com/iancaffey/logic">logic</a> runtime on request
 * and any user-defined logic models found with the {@link Logic} or {@link Include} annotations.
 * <p>
 * Types being "logically modeled" will have an entire hierarchy of {@link Predicate} generated to create an API for
 * logic programming.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"io.logic.Logic", "io.logic.Logic.Include", "io.logic.Logic.Includes"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LogicProcessor extends AbstractProcessor {
    private static final Map<Visibility, Modifier> VISIBILITY_MODIFIERS = ImmutableMap.of(
            Visibility.PUBLIC, Modifier.PUBLIC,
            Visibility.PRIVATE, Modifier.PRIVATE,
            Visibility.PROTECTED, Modifier.PROTECTED
    );
    private static final Map<TypeKind, Class<?>> PRIMITIVE_TYPES = ImmutableMap.<TypeKind, Class<?>>builder()
            .put(TypeKind.BOOLEAN, boolean.class)
            .put(TypeKind.BYTE, byte.class)
            .put(TypeKind.SHORT, short.class)
            .put(TypeKind.INT, int.class)
            .put(TypeKind.LONG, long.class)
            .put(TypeKind.CHAR, char.class)
            .put(TypeKind.FLOAT, float.class)
            .put(TypeKind.DOUBLE, double.class)
            .build();

    /**
     * Performs all {@link Logic} modeling for classes made available to the annotation processor.
     * <p>
     * All elements in the {@link RoundEnvironment} are searched for elements annotated with {@link Logic} or
     * {@link Include} and are modeled as {@link PredicateDefinition} using {@link LogicProcessor#findLogic(RoundEnvironment)}
     * and written as source files.
     *
     * @param annotations the annotation types requested to be processed
     * @param roundEnv    environment for information about the current and prior round
     * @return {@code false} to never claim the annotation types to be processed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || annotations.isEmpty()) {
            return false;
        }
        Set<PredicateDefinition> baseLogic = findLogic(roundEnv);
        Map<TypeName, ClassName> modelToPredicateName = baseLogic.stream().collect(Collectors.toMap(
                PredicateDefinition::getTypeName,
                PredicateDefinition::getPredicateName
        ));
        Set<PredicateDefinition> logic = new HashSet<>(baseLogic);
        //Manually generate predicate implementation for an array type using the existing definition as the component type
        baseLogic.forEach(definition -> {
            ArrayTypeName arrayTypeName = ArrayTypeName.of(definition.getTypeName());
            String simplePredicateName = definition.getPredicateName().simpleName();
            String simpleArrayPredicateName = simplePredicateName.substring(0, simplePredicateName.indexOf("Predicate")) + "ArrayPredicate";
            ClassName arrayPredicateName = definition.getPredicateName().peerClass(simpleArrayPredicateName);
            String arrayTypeParameterName = LogicGenerator.toParameterName(arrayTypeName);
            PredicateDefinition arrayDefinition = PredicateDefinition.builder()
                    .setTypeName(arrayTypeName)
                    .setPredicateName(arrayPredicateName)
                    .setGsonEnabled(definition.isGsonEnabled())
                    .setVisitorEnabled(definition.isVisitorEnabled())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("Empty")
                            .setFactoryName("isEmpty")
                            .setBody("$L.length == 0", arrayTypeParameterName)
                            .build())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("NonEmpty")
                            .setFactoryName("isNotEmpty")
                            .setBody("$L.length != 0", arrayTypeParameterName)
                            .build())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("Equals")
                            .setFactoryName("isEqualTo")
                            .putParameter("value", arrayTypeName)
                            .setBody("$T.equals($L, getValue())", TypeName.get(Arrays.class), arrayTypeParameterName)
                            .build())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("NotEquals")
                            .setFactoryName("isNotEqualTo")
                            .putParameter("value", arrayTypeName)
                            .setBody("!$T.equals($L, getValue())", TypeName.get(Arrays.class), arrayTypeParameterName)
                            .build())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("Length")
                            .setFactoryName("whenLength")
                            .putParameter("predicate", modelToPredicateName.get(TypeName.INT))
                            .setBody("getPredicate().test($L.length)", arrayTypeParameterName)
                            .build())
                    .addMember(MixinDefinition.builder()
                            .setPredicateName("Index")
                            .setFactoryName("whenIndex")
                            .putParameter("index", TypeName.INT)
                            .putParameter("predicate", definition.getPredicateName())
                            .setBody("getPredicate().test($L[getIndex()])", arrayTypeParameterName)
                            .build())
                    .build();
            logic.add(arrayDefinition);
        });
        LogicGenerator.generate(logic, modelToPredicateName).forEach(this::write);
        return false;
    }

    /**
     * Searches through all elements in the {@link RoundEnvironment} for elements annotated with {@link Logic} or
     * {@link Include} and creates {@link PredicateDefinition} for each found element.
     *
     * @param roundEnv the round environment used to find logic models
     * @return a set of {@link PredicateDefinition} which represents every type being modeled
     */
    @SuppressWarnings("unchecked")
    private Set<PredicateDefinition> findLogic(RoundEnvironment roundEnv) {
        TypeMirror logicTypeMirror = processingEnv.getElementUtils().getTypeElement(Logic.class.getCanonicalName()).asType();
        TypeMirror includeTypeMirror = processingEnv.getElementUtils().getTypeElement(Include.class.getCanonicalName()).asType();
        TypeMirror includesTypeMirror = processingEnv.getElementUtils().getTypeElement(Includes.class.getCanonicalName()).asType();
        ImmutableSet.Builder<PrimitiveTypeLogic> primitiveTypeLogicBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<DeclaredTypeLogic> declaredTypeLogicBuilder = ImmutableSet.builder();
        //Collect all explicit @Logic declarations from annotated classes
        roundEnv.getElementsAnnotatedWith(Logic.class).forEach(element -> {
            if (!(element instanceof TypeElement)) {
                throw new RuntimeException("Found a @Logic annotation on a non-type element.");
            }
            Logic logic = element.getAnnotation(Logic.class);
            element.getAnnotationMirrors().forEach(mirror -> {
                if (!processingEnv.getTypeUtils().isSameType(logicTypeMirror, mirror.getAnnotationType())) {
                    return;
                }
                declaredTypeLogicBuilder.add(DeclaredTypeLogic.of(element, (TypeElement) element, toLogicSpec(logic, mirror)));
            });
        });
        //Collect all implicit @Logic declarations brought in by single @Logic.Include
        roundEnv.getElementsAnnotatedWith(Include.class).forEach(element -> {
            Logic logic = element.getAnnotation(Include.class).logic();
            element.getAnnotationMirrors().forEach(mirror -> {
                if (!processingEnv.getTypeUtils().isSameType(includeTypeMirror, mirror.getAnnotationType())) {
                    return;
                }
                includeTypeLogic(element, logic, mirror, primitiveTypeLogicBuilder, declaredTypeLogicBuilder);
            });
        });
        //Collect all implicit @Logic declarations brought in by repeated @Logic.Include (note: getAnnotationsByType cannot be used here since mirrors also need to be fetched)
        roundEnv.getElementsAnnotatedWith(Includes.class).forEach(element -> {
            Include[] includes = element.getAnnotation(Includes.class).value();
            element.getAnnotationMirrors().forEach(mirror -> {
                if (!processingEnv.getTypeUtils().isSameType(includesTypeMirror, mirror.getAnnotationType())) {
                    return;
                }
                processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((key, value) -> {
                    String name = key.getSimpleName().toString();
                    if (!"value".equals(name)) {
                        return;
                    }
                    List<AnnotationMirror> included = (List<AnnotationMirror>) value.getValue();
                    for (int i = 0; i < included.size(); i++) {
                        includeTypeLogic(element, includes[i].logic(), included.get(i), primitiveTypeLogicBuilder, declaredTypeLogicBuilder);
                    }
                });
            });
        });
        //Convert all DeclaredTypeLogic -> DeclaredTypeDefinition
        Stream<PredicateDefinition> declaredTypes = declaredTypeLogicBuilder.build().stream().map(typeLogic -> {
            Element source = typeLogic.getSource();
            TypeElement type = typeLogic.getType();
            LogicSpec logic = typeLogic.getLogic();
            String namespaceOverride = logic.getNamespace();
            String namespace = namespaceOverride.isEmpty() ?
                    processingEnv.getElementUtils().getPackageOf(source).getQualifiedName().toString() :
                    namespaceOverride;
            PredicateDefinition.Builder builder = PredicateDefinition.builder()
                    .setPredicateName(ClassName.get(namespace, type.getSimpleName() + "Predicate"))
                    .setTypeName(ClassName.get(type))
                    .setGsonEnabled(logic.isGsonEnabled())
                    .setVisitorEnabled(logic.isVisitorEnabled());
            List<? extends Element> enclosedElements = type.getEnclosedElements();
            //Add all detected fields to the PredicateDefinition
            enclosedElements.stream()
                    .filter(element -> !element.getModifiers().contains(Modifier.STATIC))
                    .filter(element -> element.getAnnotation(Ignore.class) == null) //Explicitly ignored element
                    .filter(element -> element.getKind() == ElementKind.FIELD)
                    .filter(element -> element instanceof VariableElement)
                    .filter(element -> logic.getFieldVisibility().stream().anyMatch(visibility -> withinAccess(visibility, element.getModifiers())))
                    .forEach(element -> {
                        for (Pattern pattern : logic.getFieldPatterns()) {
                            Matcher matcher = pattern.matcher(element.getSimpleName());
                            if (matcher.matches()) {
                                builder.addMember(FieldDefinitionAdapter.convert(matcher.group(1), (VariableElement) element));
                            }
                        }
                    });
            //Add all detected methods to the PredicateDefinition
            enclosedElements.stream()
                    .filter(element -> !element.getModifiers().contains(Modifier.STATIC))
                    .filter(element -> element.getAnnotation(Ignore.class) == null) //Explicitly ignored element
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .filter(element -> (element instanceof ExecutableElement) && ((ExecutableElement) element).getParameters().isEmpty())
                    .filter(element -> logic.getMethodVisibility().stream().anyMatch(visibility -> withinAccess(visibility, element.getModifiers())))
                    .forEach(element -> {
                        for (Pattern pattern : logic.getMethodPatterns()) {
                            Matcher matcher = pattern.matcher(element.getSimpleName());
                            if (matcher.matches()) {
                                builder.addMember(MethodDefinitionAdapter.convert(matcher.group(1), (ExecutableElement) element));
                            }
                        }
                    });
            //Add all @Logic.Mixin to the PredicateDefinition
            for (MixinSpec mixin : logic.getMixins()) {
                builder.addMember(MixinDefinitionAdapter.convert(mixin));
            }
            return builder.build();
        });
        //Convert all PrimitiveTypeLogic -> PrimitiveTypeDefinition
        Stream<PredicateDefinition> primitiveTypes = primitiveTypeLogicBuilder.build().stream().map(typeLogic -> {
            Element source = typeLogic.getSource();
            Class<?> type = typeLogic.getType();
            LogicSpec logic = typeLogic.getLogic();
            String typeName = type.getName();
            String namespace = processingEnv.getElementUtils().getPackageOf(source).getQualifiedName().toString();
            PredicateDefinition.Builder builder = PredicateDefinition.builder()
                    .setPredicateName(ClassName.get(namespace, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, typeName) + "Predicate"))
                    .setTypeName(TypeName.get(type))
                    .setGsonEnabled(logic.isGsonEnabled())
                    .setVisitorEnabled(logic.isVisitorEnabled());
            //Add all @Logic.Mixin to the PredicateDefinition (primitive type definition only have mixins as members)
            for (MixinSpec mixin : logic.getMixins()) {
                builder.addMember(MixinDefinitionAdapter.convert(mixin));
            }
            return builder.build();
        });
        return Stream.concat(declaredTypes, primitiveTypes).collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Extracts the {@link LogicSpec} from the {@link Logic} annotation and the {@link Include} annotation mirror.
     *
     * @param source                    the originating program element for the annotation
     * @param partialLogic              the logic annotation
     * @param mirror                    the include annotation mirror
     * @param primitiveTypeLogicBuilder the builder for adding the logic if it is a primitive
     * @param declaredTypeLogicBuilder  the builder for adding the logic if it is a declared type
     */
    @SuppressWarnings("unchecked")
    private void includeTypeLogic(Element source, Logic partialLogic, AnnotationMirror mirror,
                                  ImmutableSet.Builder<PrimitiveTypeLogic> primitiveTypeLogicBuilder,
                                  ImmutableSet.Builder<DeclaredTypeLogic> declaredTypeLogicBuilder) {
        AtomicReference<LogicSpec> logicSpec = new AtomicReference<>();
        processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((key, value) -> {
            String name = key.getSimpleName().toString();
            if (!"logic".equals(name)) {
                return;
            }
            logicSpec.set(toLogicSpec(partialLogic, (AnnotationMirror) value.getValue()));
        });
        LogicSpec logic = logicSpec.get();
        processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((key, value) -> {
            String name = key.getSimpleName().toString();
            if (!"value".equals(name)) {
                return;
            }
            ((List<? extends AnnotationValue>) value.getValue()).forEach(include -> {
                TypeMirror includedType = (TypeMirror) include.getValue();
                if (includedType instanceof PrimitiveType) {
                    primitiveTypeLogicBuilder.add(PrimitiveTypeLogic.of(source, PRIMITIVE_TYPES.get(includedType.getKind()), logic));
                } else if (includedType instanceof DeclaredType) {
                    Element includedTypeElement = ((DeclaredType) includedType).asElement();
                    if (!(includedTypeElement instanceof TypeElement)) {
                        throw new RuntimeException("Found a @Logic.Include annotation on a non-type element.");
                    }
                    declaredTypeLogicBuilder.add(DeclaredTypeLogic.of(source, (TypeElement) includedTypeElement, logic));
                } else {
                    throw new RuntimeException("@Logic.Include only supports class, interface, or primitive types: " + includedType);
                }
            });
        });
    }

    /**
     * Merges the {@link Logic} annotation and its corresponding {@link AnnotationMirror} into a {@link LogicSpec}.
     * <p>
     * {@link Class} references within {@link java.lang.annotation.Annotation} require reflecting through the mirror.
     *
     * @param logic  the logic annotation
     * @param mirror the mirror of the logic annotation
     * @return a new {@link LogicSpec} that represents the specified logic annotation
     */
    @SuppressWarnings("unchecked")
    private LogicSpec toLogicSpec(Logic logic, AnnotationMirror mirror) {
        LogicSpec.Builder builder = LogicSpec.builder()
                .addFieldPatterns(Arrays.stream(logic.fields()).map(this::toPattern).toArray(Pattern[]::new))
                .addFieldVisibility(logic.fieldVisibility())
                .addMethodPatterns(Arrays.stream(logic.methods()).map(this::toPattern).toArray(Pattern[]::new))
                .addMethodVisibility(logic.methodVisibility())
                .setNamespace(logic.namespace())
                .setGsonEnabled(logic.gson())
                .setVisitorEnabled(logic.visitor());
        processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((key, value) -> {
            String name = key.getSimpleName().toString();
            if (!"mixins".equals(name)) {
                return;
            }
            ((List<? extends AnnotationValue>) value.getValue()).forEach(mixin ->
                    builder.addMixin(toMixinSpec(((AnnotationMirror) mixin.getValue())))
            );
        });
        return builder.build();
    }

    /**
     * Constructs a new {@link MixinSpec} from the specified {@link Mixin} annotation mirror.
     * <p>
     * {@link Mixin.Parameter#type()} cannot be accessed directly through the {@link Mixin} annotation provided to the
     * annotation processor, so must be reflected through the mirror.
     *
     * @param mirror the mixin annotation mirror
     * @return a new {@link MixinSpec}
     */
    @SuppressWarnings("unchecked")
    private MixinSpec toMixinSpec(AnnotationMirror mirror) {
        MixinSpec.Builder builder = MixinSpec.builder();
        processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).forEach((key, value) -> {
            String name = key.getSimpleName().toString();
            switch (name) {
                case "name":
                    builder.setName(((String) value.getValue()));
                    break;
                case "factoryName":
                    builder.setFactoryName(((String) value.getValue()));
                    break;
                case "parameters": {
                    ((List<? extends AnnotationValue>) value.getValue()).forEach(parameter -> {
                        AnnotationMirror parameterMirror = (AnnotationMirror) parameter.getValue();
                        AtomicReference<String> parameterName = new AtomicReference<>();
                        AtomicReference<TypeName> parameterType = new AtomicReference<>();
                        processingEnv.getElementUtils().getElementValuesWithDefaults(parameterMirror).forEach((parameterKey, parameterValue) -> {
                            String fieldName = parameterKey.getSimpleName().toString();
                            switch (fieldName) {
                                case "name":
                                    parameterName.set(((String) parameterValue.getValue()));
                                    break;
                                case "type":
                                    parameterType.set(TypeName.get((TypeMirror) parameterValue.getValue()));
                                    break;
                            }
                        });
                        builder.putParameter(parameterName.get(), parameterType.get());
                    });
                    break;
                }
                case "expression":
                    builder.setExpression(((String) value.getValue()));
                    break;
                case "arguments": {
                    ((List<? extends AnnotationValue>) value.getValue()).forEach(argument -> {
                        AnnotationMirror argumentMirror = (AnnotationMirror) argument.getValue();
                        AtomicReference<String> stringValue = new AtomicReference<>();
                        AtomicReference<TypeName> typeNameValue = new AtomicReference<>();
                        processingEnv.getElementUtils().getElementValuesWithDefaults(argumentMirror).forEach((argumentKey, argumentValue) -> {
                            String fieldName = argumentKey.getSimpleName().toString();
                            switch (fieldName) {
                                case "value":
                                    stringValue.set(((String) argumentValue.getValue()));
                                    break;
                                case "type":
                                    typeNameValue.set(TypeName.get((TypeMirror) argumentValue.getValue()));
                                    break;
                            }
                        });
                        //if the string value is non-empty, use it; otherwise, use the type name
                        String string = stringValue.get();
                        builder.addArgument(string.isEmpty() ? typeNameValue.get() : string);
                    });
                    break;
                }
            }
        });
        return builder.build();
    }

    /**
     * Tests whether the specified set of {@link Modifier} are within the scope of the member {@link Visibility}.
     * <p>
     * Access level {@link Modifier} (e.g. {@link Modifier#PRIVATE}, {@link Modifier#PUBLIC}, {@link Modifier#PRIVATE},
     * {@link Modifier#PROTECTED})
     * are mapped to the corresponding {@link Visibility}, if it exists. If no access level modifiers are present,
     * {@link Visibility#PACKAGE} is inferred and tested against the specified visibility.
     *
     * @param visibility the allowed access level
     * @param modifiers  the set of modifiers which can contain access level modifiers
     * @return {@code true} if the specified visibility corresponds to the relevant access modifier inferred from the provided modifiers
     */
    private boolean withinAccess(Visibility visibility, Set<Modifier> modifiers) {
        switch (visibility) {
            case PUBLIC:
            case PRIVATE:
            case PROTECTED:
                return modifiers.contains(VISIBILITY_MODIFIERS.get(visibility));
            case PACKAGE:
                //package-local elements will have none of the explicit access-level modifiers
                return VISIBILITY_MODIFIERS.values().stream().noneMatch(modifiers::contains);
            default:
                throw new UnsupportedOperationException("Unsupported visibility level " + visibility);
        }
    }

    /**
     * Converts a {@link Logic} member matcher expression into a {@link Pattern}. Every {@code *} is replaced by a
     * non-empty capturing group {@code (.+)}.
     *
     * @param expression the logic member matcher expression
     * @return a {@link Pattern} that represents the logic member matcher expression
     */
    private Pattern toPattern(String expression) {
        return Pattern.compile(expression.replace("*", "(.+)"));
    }

    /**
     * Writes the {@link JavaFile} out to the {@link ProcessingEnvironment#getFiler()}.
     *
     * @param file the file to write
     * @throws IllegalArgumentException if an exception occurs when writing the file
     */
    private void write(JavaFile file) {
        try {
            file.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write Logic file.", e);
        }
    }
}
