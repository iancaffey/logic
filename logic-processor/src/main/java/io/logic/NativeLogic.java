package io.logic;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A utility class for generating the {@link PredicateDefinition} for the
 * <a href="https://github.com/iancaffey/logic">logic</a> runtime.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@UtilityClass
public class NativeLogic {
    private static final ClassName BASE = ClassName.get(Logic.class); //used to find the base package of native predicate implementations

    /**
     * Returns the mapping of predicate model type name to predicate type name for all native datatypes handled by the
     * <a href="https://github.com/iancaffey/logic">logic</a> runtime.
     *
     * @return the model to predicate name mapping for the logic runtime
     */
    public Map<TypeName, ClassName> getPredicateNames() {
        return Stream.of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, String.class)
                .collect(ImmutableMap.toImmutableMap(TypeName::get,
                        type -> ClassName.get(BASE.packageName(), CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type.getSimpleName()) + "Predicate")
                ));
    }

    /**
     * Returns the {@link PredicateDefinition} for all native datatypes handled by the
     * <a href="https://github.com/iancaffey/logic">logic</a> runtime.
     *
     * @return the logic runtime predicate definitions
     */
    public Set<PredicateDefinition> getPredicates() {
        return Stream.concat(
                Stream.of(forBoolean(), forCharacter(), forString()),
                Stream.of(byte.class, short.class, int.class, long.class, float.class, double.class).map(NativeLogic::forNumber)
        ).collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Constructs a new {@link PredicateDefinition} for the {@link String} datatype.
     * <p>
     * The following predicate implementations are supported:
     * <ul>
     * <li>Equals</li>
     * <li>NotEquals</li>
     * <li>EqualsIgnoreCase</li>
     * <li>Empty</li>
     * <li>NonEmpty</li>
     * <li>Matches</li>
     * <li>Contains</li>
     * </ul>
     *
     * @return a new {@link PredicateDefinition}
     */
    private PredicateDefinition forString() {
        return PredicateDefinition.builder()
                .setPredicateName(BASE.peerClass("StringPredicate"))
                .setTypeName(ClassName.get(String.class))
                .addMember(MixinDefinition.builder()
                        .setPredicateName("Equals")
                        .setFactoryName("equalTo")
                        .putParameter("value", String.class)
                        .setBody("string.equals(getValue())")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("NotEquals")
                        .setFactoryName("notEqualTo")
                        .putParameter("value", String.class)
                        .setBody("!string.equals(getValue())")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("EqualsIgnoreCase")
                        .setFactoryName("equalsIgnoreCase")
                        .putParameter("value", String.class)
                        .setBody("string.equalsIgnoreCase(getValue())")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("Empty")
                        .setFactoryName("isEmpty")
                        .setBody("string.isEmpty()")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("NonEmpty")
                        .setFactoryName("isNotEmpty")
                        .setBody("!string.isEmpty()")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("Matches")
                        .setFactoryName("matches")
                        .putParameter("pattern", Pattern.class)
                        .setBody("getPattern().matcher(string).matches()")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("Contains")
                        .setFactoryName("contains")
                        .putParameter("value", String.class)
                        .setBody("string.contains(getValue())")
                        .build())
                .setGsonEnabled(true)
                .setVisitorEnabled(true)
                .build();
    }

    /**
     * Constructs a new {@link PredicateDefinition} for the {@code boolean} datatype.
     * <p>
     * The following predicate implementations are supported:
     * <ul>
     * <li>True</li>
     * <li>False</li>
     * </ul>
     *
     * @return a new {@link PredicateDefinition}
     */
    private PredicateDefinition forBoolean() {
        return forPrimitive(boolean.class)
                .addMember(MixinDefinition.builder()
                        .setPredicateName("True")
                        .setFactoryName("isTrue")
                        .setBody("b")
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("False")
                        .setFactoryName("isFalse")
                        .setBody("!b")
                        .build())
                .build();
    }

    /**
     * Constructs a new {@link PredicateDefinition} for the {@code char} datatype.
     * <p>
     * The following predicate implementations are supported:
     * <ul>
     * <li>Equals</li>
     * <li>NotEquals</li>
     * <li>LessThan</li>
     * <li>LessThanEquals</li>
     * <li>GreaterThan</li>
     * <li>GreaterThanEquals</li>
     * <li>UpperCase</li>
     * <li>LowerCase</li>
     * </ul>
     *
     * @return a new {@link PredicateDefinition}
     */
    private PredicateDefinition forCharacter() {
        return PredicateDefinition.builder().from(forNumber(char.class))
                .addMember(MixinDefinition.builder()
                        .setPredicateName("UpperCase")
                        .setFactoryName("isUpperCase")
                        .setBody("$T.isUpperCase(c)", Character.class)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("LowerCase")
                        .setFactoryName("isLowerCase")
                        .setBody("$T.isLowerCase(c)", Character.class)
                        .build())
                .build();
    }

    /**
     * Constructs a new {@link PredicateDefinition} for a numeric primitive datatype.
     * <p>
     * The following predicate implementations are supported:
     * <ul>
     * <li>Equals</li>
     * <li>NotEquals</li>
     * <li>LessThan</li>
     * <li>LessThanEquals</li>
     * <li>GreaterThan</li>
     * <li>GreaterThanEquals</li>
     * </ul>
     *
     * @return a new {@link PredicateDefinition}
     * @throws IllegalArgumentException if the type is not primitive
     */
    private PredicateDefinition forNumber(@NonNull Class<?> type) {
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("Expected a primitive type.");
        }
        char argumentName = Character.toLowerCase(type.getName().charAt(0));
        return forPrimitive(type)
                .addMember(MixinDefinition.builder()
                        .setPredicateName("Equals")
                        .setFactoryName("equalTo")
                        .putParameter("value", type)
                        .setBody("$L == getValue()", argumentName)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("NotEquals")
                        .setFactoryName("notEqualTo")
                        .putParameter("value", type)
                        .setBody("$L != getValue()", argumentName)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("LessThan")
                        .setFactoryName("lessThan")
                        .putParameter("value", type)
                        .setBody("$L < getValue()", argumentName)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("LessThanEquals")
                        .setFactoryName("lessThanEquals")
                        .putParameter("value", type)
                        .setBody("$L <= getValue()", argumentName)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("GreaterThan")
                        .setFactoryName("greaterThan")
                        .putParameter("value", type)
                        .setBody("$L > getValue()", argumentName)
                        .build())
                .addMember(MixinDefinition.builder()
                        .setPredicateName("GreaterThanEquals")
                        .setFactoryName("greaterThanEquals")
                        .putParameter("value", type)
                        .setBody("$L >= getValue()", argumentName)
                        .build())
                .build();
    }

    /**
     * Constructs the base {@link PredicateDefinition.Builder} for building any primitive definition.
     *
     * @param type the primitive type being defined
     * @return a new {@link PredicateDefinition.Builder}
     */
    private PredicateDefinition.Builder forPrimitive(@NonNull Class<?> type) {
        return PredicateDefinition.builder()
                .setPredicateName(BASE.peerClass(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, type.getName()) + "Predicate"))
                .setGsonEnabled(true)
                .setVisitorEnabled(true)
                .setTypeName(TypeName.get(type));
    }
}
