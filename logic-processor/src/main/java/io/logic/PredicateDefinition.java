package io.logic;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.logic.Logic.Include;
import io.logic.Logic.Mixin;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Immutable;

import java.util.Set;

/**
 * A representation of the definition for a predicate being generated for a type in the Java programming language.
 * <p>
 * {@link PredicateDefinition} can represent both primitive and declared types (class and interface types).
 * <p>
 * {@link PredicateDefinition} for a primitive type is dictated by a {@link Logic} annotation referenced in an
 * {@link Include} annotation.
 * <p>
 * {@link PredicateDefinition} for a class or interface type is dictated by a {@link Logic} annotation placed on the type
 * or included from another context by {@link Include}.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface PredicateDefinition {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutablePredicateDefinition.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static PredicateDefinition of(ClassName predicateName, TypeName typeName, Set<? extends MemberDefinition> members, boolean gsonEnabled, boolean visitorEnabled) {
        return ImmutablePredicateDefinition.of(predicateName, typeName, members, gsonEnabled, visitorEnabled);
    }

    /**
     * Represents the class name of the predicate.
     * <p>
     * When building predicate implementations, this value is used as the class name of the nested class that represents
     * the predicate implementation for this member.
     *
     * @return the class name of the predicate
     */
    ClassName getPredicateName();

    /**
     * Represents the type name of the model being logically defined.
     *
     * @return the type name of the model
     */
    TypeName getTypeName();

    /**
     * Represents the predicate implementation definitions for members of the model.
     * <p>
     * Predicate implementations can perform checks on fields, methods, or custom {@link Mixin} defined on the model itself.
     *
     * @return the member predicate definitions
     */
    Set<MemberDefinition> getMembers();

    /**
     * Represents whether {@link Gson} {@link TypeAdapter} should be generated for the predicate hierarchy.
     *
     * @return whether gson type adapters are generated for the predicate implementations
     */
    boolean isGsonEnabled();

    /**
     * Represents whether the visitor pattern will be implemented for the predicate hierarchy.
     *
     * @return whether visitors are generated for the predicates
     */
    boolean isVisitorEnabled();

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder from(PredicateDefinition definition);

        Builder setPredicateName(ClassName predicateName);

        Builder setTypeName(TypeName typeName);

        Builder addMember(MemberDefinition element);

        Builder addMembers(MemberDefinition... elements);

        Builder setMembers(Iterable<? extends MemberDefinition> elements);

        Builder addAllMembers(Iterable<? extends MemberDefinition> elements);

        Builder setGsonEnabled(boolean gsonEnabled);

        Builder setVisitorEnabled(boolean visitorEnabled);

        PredicateDefinition build();
    }
}
