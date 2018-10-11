package io.logic;

import com.squareup.javapoet.TypeName;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

/**
 * A representation of the definition of a method in a {@link PredicateDefinition}.
 * <p>
 * {@link MethodDefinition} provides basic name and type information for the referenced method along with naming information
 * for generating the predicate implementation for the member.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface MethodDefinition extends MemberDefinition {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutableMethodDefinition.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static MethodDefinition of(String name, TypeName returnType, String predicateName) {
        return ImmutableMethodDefinition.of(name, returnType, predicateName);
    }

    /**
     * Represents the name of the method that the definition corresponds to.
     *
     * @return the definition method name
     */
    String getName();

    /**
     * Represents the type name of the return type of the method that the definition corresponds to.
     *
     * @return the definition method return type name
     */
    TypeName getReturnType();

    /**
     * Represents the predicate name of the method definition.
     * <p>
     * When building predicate implementations, this value is used as the class name of the nested class that represents
     * the predicate implementation for this method.
     *
     * @return the class name of the method definition predicate
     */
    @Override
    String getPredicateName();

    /**
     * Represents the factory name of the method definition predicate.
     * <p>
     * After building the predicate implementation for this member, a static factory method is added to the parent
     * predicate class for creating this method's predicate.
     *
     * @return the predicate name prefixed with {@code when}
     */
    @Derived
    @Override
    default String getFactoryName() {
        return "when" + getPredicateName();
    }

    /**
     * Accepts a {@link MemberDefinitionVisitor} to visit an implementation of {@link MemberDefinition}.
     * <p>
     * {@link MemberDefinitionVisitor} is forwarded to {@link MemberDefinitionVisitor#visit(MethodDefinition)}
     * and the result of invoking the visit method is returned.
     *
     * @param visitor the visitor to accept
     * @param <T>     the return type of the visitation of a method definition
     * @return the value the visitor produces after visiting a method definition
     */
    @Override
    default <T> T accept(MemberDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder from(MethodDefinition definition);

        Builder setName(String name);

        Builder setReturnType(TypeName returnType);

        Builder setPredicateName(String predicateName);

        MethodDefinition build();
    }
}
