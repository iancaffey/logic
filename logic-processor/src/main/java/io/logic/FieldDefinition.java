package io.logic;

import com.squareup.javapoet.TypeName;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

/**
 * A representation of the definition of a field in a {@link PredicateDefinition}.
 * <p>
 * {@link FieldDefinition} provides basic name and type information for the referenced field along with naming information
 * for generating the predicate implementation for the member.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface FieldDefinition extends MemberDefinition {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutableFieldDefinition.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static FieldDefinition of(String name, TypeName type, String predicateName) {
        return ImmutableFieldDefinition.of(name, type, predicateName);
    }

    /**
     * Represents the name of the field that the definition corresponds to.
     *
     * @return the definition field name
     */
    String getName();

    /**
     * Represents the type name of the field that the definition corresponds to.
     *
     * @return the definition field type name
     */
    TypeName getType();

    /**
     * Represents the predicate name of the field definition.
     * <p>
     * When building predicate implementations, this value is used as the class name of the nested class that represents
     * the predicate implementation for this field.
     *
     * @return the class name of the field definition predicate
     */
    @Override
    String getPredicateName();

    /**
     * Represents the factory name of the field definition predicate.
     * <p>
     * After building the predicate implementation for this member, a static factory method is added to the parent
     * predicate class for creating this field's predicate.
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
     * {@link MemberDefinitionVisitor} is forwarded to {@link MemberDefinitionVisitor#visit(FieldDefinition)}
     * and the result of invoking the visit method is returned.
     *
     * @param visitor the visitor to accept
     * @param <T>     the return type of the visitation of a field definition
     * @return the value the visitor produces after visiting a field definition
     */
    @Override
    default <T> T accept(MemberDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder from(FieldDefinition definition);

        Builder setName(String name);

        Builder setType(TypeName type);

        Builder setPredicateName(String predicateName);

        FieldDefinition build();
    }
}
