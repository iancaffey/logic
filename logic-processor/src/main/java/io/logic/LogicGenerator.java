package io.logic;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.squareup.javapoet.*;
import io.logic.gson.TypeAdapterFactoryMirror;
import io.logic.immutables.ImmutableLogicStyle;
import lombok.experimental.UtilityClass;
import org.immutables.gson.Gson;
import org.immutables.metainf.Metainf;
import org.immutables.value.Value.Enclosing;
import org.immutables.value.Value.Immutable;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for performing code generation for {@link LogicProcessor}.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@UtilityClass
public class LogicGenerator {
    private static final String INDENT = "    "; //we need to keep track of the indent for some custom formatting
    private final AnnotationSpec GENERATED = AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", LogicGenerator.class.getName())
            .build();
    private static final Set<String> RESERVED_WORDS = ImmutableSet.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default",
            "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "void", "volatile", "while"
    );
    private static final Map<TypeName, Class<?>> PREDICATE_SPECIALIZATIONS = ImmutableMap.of(
            TypeName.INT, IntPredicate.class,
            TypeName.DOUBLE, DoublePredicate.class,
            TypeName.LONG, LongPredicate.class
    );

    /**
     * Generates all source files for the specified {@link PredicateDefinition}.
     *
     * @param definitions the predicate definitions to construct
     * @return a set of {@link JavaFile} that contains every predicate hierarchy, visitor, and type adapter factory
     */
    public Set<JavaFile> generate(Set<PredicateDefinition> definitions) {
        Map<TypeName, ClassName> modelToPredicateName = definitions.stream().collect(Collectors.toMap(
                PredicateDefinition::getTypeName,
                PredicateDefinition::getPredicateName
        ));
        ImmutableSet.Builder<JavaFile> builder = ImmutableSet.builder();
        definitions.forEach(definition -> {
            ClassName predicateName = definition.getPredicateName();
            builder.add(createFile(predicateName.packageName(), createPredicate(definition, modelToPredicateName)));
            if (definition.gsonEnabled()) {
                builder.add(createFile(predicateName.packageName(), createTypeAdapterFactory(definition)));
            }
            if (definition.visitorEnabled()) {
                builder.add(createFile(predicateName.packageName(), createVisitor(definition)));
            }
        });
        return builder.build();
    }

    /**
     * Constructs the {@link TypeSpec} that corresponds to the {@link com.google.gson.TypeAdapterFactory} of the {@link PredicateDefinition} model.
     *
     * @param definition the predicate definition to construct the type adapter factory
     * @return a new {@link TypeSpec} that represents the predicate type adapter factory
     */
    private TypeSpec createTypeAdapterFactory(PredicateDefinition definition) {
        ClassName predicateName = definition.getPredicateName();
        Set<? extends MemberDefinition> members = definition.getMembers();
        ClassName immutableEnclosingTypeName = predicateName.peerClass("Immutable" + predicateName.simpleName());
        ParameterizedTypeName delegateTypeName = ParameterizedTypeName.get(ClassName.get(RuntimeTypeAdapterFactory.class), predicateName);
        Set<ClassName> nestedPredicateNames = Stream.concat(
                Stream.of("And", "Or", "Not").map(immutableEnclosingTypeName::nestedClass),
                members.stream().map(member -> immutableEnclosingTypeName.nestedClass(member.getPredicateName()))
        ).collect(ImmutableSet.toImmutableSet());
        String delegateFactoryInitializer = nestedPredicateNames.stream()
                .map(d -> "\n" + INDENT + INDENT + ".registerSubtype($T.class)")
                .collect(Collectors.joining("", "RuntimeTypeAdapterFactory.of($T.class)", ""));
        return TypeSpec.classBuilder(ClassName.get(predicateName.packageName(), predicateName.simpleName() + "TypeAdapterFactory"))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Metainf.Service.class)
                .addAnnotation(GENERATED)
                .addSuperinterface(TypeAdapterFactoryMirror.class)
                .addField(FieldSpec.builder(delegateTypeName, "delegate", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer(delegateFactoryInitializer, Stream.concat(Stream.of(predicateName), nestedPredicateNames.stream()).toArray())
                        .build())
                .addMethod(MethodSpec.methodBuilder("getFactory")
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return delegate")
                        .returns(TypeAdapterFactory.class)
                        .build())
                .build();
    }

    /**
     * Constructs the {@link TypeSpec} that corresponds to the visitor interface of the {@link PredicateDefinition} model.
     *
     * @param definition the predicate definition to construct the visitor
     * @return a new {@link TypeSpec} that represents the predicate visitor
     */
    private TypeSpec createVisitor(PredicateDefinition definition) {
        ClassName predicateName = definition.getPredicateName();
        Set<? extends MemberDefinition> members = definition.getMembers();
        Set<ClassName> nestedPredicateNames = Stream.concat(
                Stream.of(predicateName.nestedClass("And"), predicateName.nestedClass("Or"), predicateName.nestedClass("Not")),
                members.stream().map(member -> predicateName.nestedClass(member.getPredicateName()))
        ).collect(ImmutableSet.toImmutableSet());
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(predicateName.peerClass(predicateName.simpleName() + "Visitor"))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GENERATED);
        TypeVariableName visitReturnType = TypeVariableName.get("T");
        builder.addTypeVariable(visitReturnType);
        nestedPredicateNames.forEach(nestedPredicateName -> {
            builder.addMethod(MethodSpec.methodBuilder("visit")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(nestedPredicateName, toParameterName(nestedPredicateName))
                    .returns(visitReturnType)
                    .build());
        });
        return builder.build();
    }

    /**
     * Constructs the {@link TypeSpec} that corresponds to the {@link PredicateDefinition} model using the specified
     * model to predicate names to resolve any delegate/references model predicates.
     *
     * @param definition           the predicate definition to construct
     * @param modelToPredicateName the mapping of model type name to predicate name
     * @return a new {@link TypeSpec} that represents the constructed predicate definition
     */
    private TypeSpec createPredicate(PredicateDefinition definition, Map<TypeName, ClassName> modelToPredicateName) {
        ClassName predicateName = definition.getPredicateName();
        TypeName modelName = definition.getTypeName();
        String modelParameterName = toParameterName(modelName);
        ClassName immutableEnclosingTypeName = predicateName.peerClass("Immutable" + predicateName.simpleName());
        boolean visitorEnabled = definition.visitorEnabled();
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(predicateName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Enclosing.class)
                .addAnnotation(ImmutableLogicStyle.class)
                .addAnnotation(GENERATED);
        if (definition.gsonEnabled()) {
            builder.addAnnotation(Gson.TypeAdapters.class);
        }
        boolean hasJava8Superinterface = false;
        if (modelName.isPrimitive()) {
            Class<?> predicate = PREDICATE_SPECIALIZATIONS.get(modelName);
            if (predicate != null) {
                builder.addSuperinterface(predicate);
                hasJava8Superinterface = true;
            }
        } else {
            builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(Predicate.class), definition.getTypeName()));
            hasJava8Superinterface = true;
        }
        //Add missing boolean test(Model model) method for types which do not have a Java8 superinterface
        if (!hasJava8Superinterface) {
            builder.addMethod(MethodSpec.methodBuilder("test")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(modelName, modelParameterName)
                    .returns(TypeName.BOOLEAN)
                    .build());
        }
        //Add support for the boxed primitive for the entire predicate hierarchy
        if (modelName.isPrimitive()) {
            TypeName boxedTypeName = modelName.box();
            String boxedParameterName = toParameterName(boxedTypeName);
            builder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(Predicate.class), boxedTypeName));
            builder.addMethod(MethodSpec.methodBuilder("test")
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .addParameter(boxedTypeName, boxedParameterName)
                    .addStatement("return test(($T) $L)", modelName, boxedParameterName)
                    .returns(TypeName.BOOLEAN)
                    .build());
        }
        ClassName andTypeName = predicateName.nestedClass("And");
        ClassName immutableAndTypName = immutableEnclosingTypeName.nestedClass("And");
        ClassName orTypeName = predicateName.nestedClass("Or");
        ClassName immutableOrTypName = immutableEnclosingTypeName.nestedClass("Or");
        ClassName notTypeName = predicateName.nestedClass("Not");
        ClassName immutableNotTypeName = immutableEnclosingTypeName.nestedClass("Not");
        //And predicate implementation
        TypeSpec.Builder andBuilder = TypeSpec.interfaceBuilder(andTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Immutable.class)
                .addSuperinterface(predicateName)
                .addMethod(MethodSpec.methodBuilder("getLeft")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getRight")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("negate")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addStatement("return getLeft().negate().or(getRight().negate())")
                        .returns(orTypeName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("test")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addParameter(modelName, modelParameterName)
                        .addStatement("return getLeft().test($1L) && getRight().test($1L)", modelParameterName)
                        .returns(TypeName.BOOLEAN)
                        .build());
        //Or predicate implementation
        TypeSpec.Builder orBuilder = TypeSpec.interfaceBuilder(orTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Immutable.class)
                .addSuperinterface(predicateName)
                .addMethod(MethodSpec.methodBuilder("getLeft")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getRight")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("negate")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addStatement("return getLeft().negate().and(getRight().negate())")
                        .returns(andTypeName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("test")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addParameter(modelName, modelParameterName)
                        .addStatement("return getLeft().test($1L) || getRight().test($1L)", modelParameterName)
                        .returns(TypeName.BOOLEAN)
                        .build());
        //Not predicate implementation
        TypeSpec.Builder notBuilder = TypeSpec.interfaceBuilder(notTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Immutable.class)
                .addSuperinterface(predicateName)
                .addMethod(MethodSpec.methodBuilder("getPredicate")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("negate")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addStatement("return getPredicate()")
                        .returns(predicateName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("test")
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addAnnotation(Override.class)
                        .addParameter(modelName, modelParameterName)
                        .addStatement("return !getPredicate().test($L)", modelParameterName)
                        .returns(TypeName.BOOLEAN)
                        .build());
        //#accept(...)
        if (visitorEnabled) {
            TypeVariableName visitorTypeVariable = TypeVariableName.get("T");
            builder.addMethod(MethodSpec.methodBuilder("accept")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addTypeVariable(visitorTypeVariable)
                    .addParameter(ParameterizedTypeName.get(predicateName.peerClass(predicateName.simpleName() + "Visitor"), visitorTypeVariable), "visitor")
                    .returns(visitorTypeVariable)
                    .build());
            MethodSpec accept = MethodSpec.methodBuilder("accept")
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .addAnnotation(Override.class)
                    .addTypeVariable(visitorTypeVariable)
                    .addParameter(ParameterizedTypeName.get(predicateName.peerClass(predicateName.simpleName() + "Visitor"), visitorTypeVariable), "visitor")
                    .addStatement("return visitor.visit(this)")
                    .returns(visitorTypeVariable)
                    .build();
            andBuilder.addMethod(accept);
            orBuilder.addMethod(accept);
            notBuilder.addMethod(accept);
        }
        builder.addType(andBuilder.build());
        builder.addType(orBuilder.build());
        builder.addType(notBuilder.build());
        //#negate()
        MethodSpec.Builder negateBuilder = MethodSpec.methodBuilder("negate")
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .addStatement("return $T.of(this)", immutableNotTypeName)
                .returns(predicateName);
        if (hasJava8Superinterface) {
            negateBuilder.addAnnotation(Override.class);
        }
        builder.addMethod(negateBuilder.build());
        //#and(...)
        builder.addMethod(MethodSpec.methodBuilder("and")
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .addParameter(predicateName, "other")
                .addStatement("return $T.of(this, other)", immutableAndTypName)
                .returns(andTypeName)
                .build());
        //#or(...)
        builder.addMethod(MethodSpec.methodBuilder("or")
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .addParameter(predicateName, "other")
                .addStatement("return $T.of(this, other)", immutableOrTypName)
                .returns(orTypeName)
                .build());
        //Member predicate implementations
        definition.getMembers().forEach(member -> {
            builder.addMethod(member.accept(new MemberDefinitionVisitor<MethodSpec>() {
                @Override
                public MethodSpec visit(FieldDefinition definition) {
                    TypeName typeName = definition.accept(new MemberTypeName());
                    ClassName memberPredicateName = modelToPredicateName.get(typeName);
                    if (memberPredicateName == null) {
                        throw new IllegalArgumentException("Unable to find predicate implementation for " + typeName + ".");
                    }
                    return MethodSpec.methodBuilder(member.getFactoryName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(memberPredicateName, "predicate")
                            .addStatement("return $T.of(predicate)", immutableEnclosingTypeName.nestedClass(member.getPredicateName()))
                            .returns(predicateName.nestedClass(member.getPredicateName()))
                            .build();
                }

                @Override
                public MethodSpec visit(MethodDefinition definition) {
                    TypeName typeName = definition.accept(new MemberTypeName());
                    ClassName memberPredicateName = modelToPredicateName.get(typeName);
                    if (memberPredicateName == null) {
                        throw new IllegalArgumentException("Unable to find predicate implementation for " + typeName + ".");
                    }
                    return MethodSpec.methodBuilder(member.getFactoryName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(memberPredicateName, "predicate")
                            .addStatement("return $T.of(predicate)", immutableEnclosingTypeName.nestedClass(member.getPredicateName()))
                            .returns(predicateName.nestedClass(member.getPredicateName()))
                            .build();
                }

                @Override
                public MethodSpec visit(MixinDefinition definition) {
                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(member.getFactoryName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(predicateName.nestedClass(member.getPredicateName()));
                    definition.getParameters().forEach((name, type) -> methodBuilder.addParameter(type, name));
                    String constructorArgumentFormatString = definition.getParameters().entrySet().stream()
                            .map(parameter -> "$L")
                            .collect(Collectors.joining(", ", "return $T.of(", ")"));
                    Object[] constructorArguments = Stream.concat(
                            Stream.of(immutableEnclosingTypeName.nestedClass(definition.getPredicateName())),
                            definition.getParameters().keySet().stream()
                    ).toArray();
                    methodBuilder.addStatement(constructorArgumentFormatString, constructorArguments);
                    return methodBuilder.build();
                }
            }));
            builder.addType(member.accept(new MemberDefinitionVisitor<TypeSpec>() {
                @Override
                public TypeSpec visit(FieldDefinition definition) {
                    return visit(definition, FieldDefinition::getName);
                }

                @Override
                public TypeSpec visit(MethodDefinition definition) {
                    return visit(definition, d -> d.getName() + "()");
                }

                @Override
                public TypeSpec visit(MixinDefinition definition) {
                    TypeSpec.Builder memberPredicateBuilder = TypeSpec.interfaceBuilder(predicateName.nestedClass(definition.getPredicateName()))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addAnnotation(Immutable.class)
                            .addSuperinterface(predicateName);
                    definition.getParameters().forEach((name, type) -> {
                        String getterName = "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
                        memberPredicateBuilder.addMethod(MethodSpec.methodBuilder(getterName)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(type)
                                .build());
                    });
                    if (visitorEnabled) {
                        TypeVariableName visitorTypeVariable = TypeVariableName.get("T");
                        memberPredicateBuilder.addMethod(MethodSpec.methodBuilder("accept")
                                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                                .addAnnotation(Override.class)
                                .addTypeVariable(visitorTypeVariable)
                                .addParameter(ParameterizedTypeName.get(predicateName.peerClass(predicateName.simpleName() + "Visitor"), visitorTypeVariable), "visitor")
                                .addStatement("return visitor.visit(this)")
                                .returns(visitorTypeVariable)
                                .build());
                    }
                    return memberPredicateBuilder.addMethod(MethodSpec.methodBuilder("test")
                            .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                            .addAnnotation(Override.class)
                            .addParameter(modelName, modelParameterName)
                            .addStatement(definition.getBody())
                            .returns(TypeName.BOOLEAN)
                            .build())
                            .build();
                }

                private <T extends MemberDefinition> TypeSpec visit(T definition, Function<T, ?> value) {
                    TypeName typeName = definition.accept(new MemberTypeName());
                    ClassName memberPredicateName = modelToPredicateName.get(typeName);
                    if (memberPredicateName == null) {
                        throw new IllegalArgumentException("Unable to find predicate implementation for " + typeName + ".");
                    }
                    TypeSpec.Builder memberPredicateBuilder = TypeSpec.interfaceBuilder(predicateName.nestedClass(definition.getPredicateName()))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addAnnotation(Immutable.class)
                            .addSuperinterface(predicateName)
                            .addMethod(MethodSpec.methodBuilder("getPredicate")
                                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                    .returns(memberPredicateName)
                                    .build())
                            .addMethod(MethodSpec.methodBuilder("test")
                                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                                    .addAnnotation(Override.class)
                                    .addParameter(modelName, modelParameterName)
                                    .addStatement("return getPredicate().test($L.$L)", modelParameterName, value.apply(definition))
                                    .returns(TypeName.BOOLEAN)
                                    .build());
                    if (visitorEnabled) {
                        TypeVariableName visitorTypeVariable = TypeVariableName.get("T");
                        memberPredicateBuilder.addMethod(MethodSpec.methodBuilder("accept")
                                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                                .addAnnotation(Override.class)
                                .addTypeVariable(visitorTypeVariable)
                                .addParameter(ParameterizedTypeName.get(predicateName.peerClass(predicateName.simpleName() + "Visitor"), visitorTypeVariable), "visitor")
                                .addStatement("return visitor.visit(this)")
                                .returns(visitorTypeVariable)
                                .build());
                    }
                    return memberPredicateBuilder.build();
                }
            }));
        });
        return builder.build();
    }

    /**
     * Converts the type name into a well formed parameter name.
     * <p>
     * The type name is converted into mixed case form. If the mixed case form is one of Java's reserved words {@link LogicGenerator#RESERVED_WORDS},
     * the first character of the type mixed case type name is used as the type name.
     *
     * @param typeName the name of the type to create a parameter name
     * @return the parameter name for the specified type name
     */
    private String toParameterName(TypeName typeName) {
        boolean plural = typeName instanceof ArrayTypeName;
        if (plural) {
            typeName = ((ArrayTypeName) typeName).componentType;
        }
        String baseName = typeName instanceof ClassName ? ((ClassName) typeName).simpleName() : typeName.toString();
        if (plural) {
            baseName += "s";
        }
        return escape(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, baseName));
    }

    /**
     * Escapes the specified name if it interferes with one of Java's reserved words, {@link LogicGenerator#RESERVED_WORDS}.
     * <p>
     * If the specified name is one of Java's reserved words the first character of the name is returned. Otherwise, the
     * specified name is returned.
     *
     * @param name the name to escape
     * @return a safe escaped name
     */
    private String escape(String name) {
        return RESERVED_WORDS.contains(name) ? name.substring(0, 1) : name;
    }

    /**
     * Constructs the top-level Java class file for the {@link TypeSpec}.
     *
     * @param packageName the package of the type spec
     * @param typeSpec    the type spec to create a source file
     * @return a new {@link JavaFile} that represents the type spec
     */
    private JavaFile createFile(String packageName, TypeSpec typeSpec) {
        return JavaFile.builder(packageName, typeSpec).indent(INDENT).skipJavaLangImports(true).build();
    }

    /**
     * An implementation of {@link MemberDefinitionVisitor} which converts the definition to its appropriate model type name.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    private static class MemberTypeName implements MemberDefinitionVisitor<TypeName> {
        /**
         * Visits the {@link FieldDefinition}.
         *
         * @param definition the definition to visit
         * @return {@link FieldDefinition#getType()}
         */
        @Override
        public TypeName visit(FieldDefinition definition) {
            return definition.getType();
        }

        /**
         * Visits the {@link MethodDefinition}.
         *
         * @param definition the definition to visit
         * @return {@link MethodDefinition#getReturnType()}
         */
        @Override
        public TypeName visit(MethodDefinition definition) {
            return definition.getReturnType();
        }

        /**
         * Visits the {@link MixinDefinition}.
         *
         * @param definition the definition to visit
         * @throws IllegalArgumentException because mixin definitions do not have a "model" type
         */
        @Override
        public TypeName visit(MixinDefinition definition) {
            throw new IllegalArgumentException("Mixins are not members and do not have type names.");
        }
    }
}
