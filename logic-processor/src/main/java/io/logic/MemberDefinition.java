package io.logic;

/**
 * A representation of the definition of a member in a {@link PredicateDefinition}.
 * <p>
 * {@link MemberDefinition} comprises the components of a parent {@link PredicateDefinition} which is used to build
 * predicate implementations for each member.
 *
 * @author Ian Caffey
 * @since 1.0
 */
public interface MemberDefinition {
    /**
     * Represents the predicate name of the member.
     * <p>
     * When building predicate implementations, this value is used as the class name of the nested class that represents
     * the predicate implementation for this member.
     *
     * @return the class name of the member predicate
     */
    String getPredicateName();

    /**
     * Represents the factory name of the member.
     * <p>
     * After building the predicate implementation for this member, a static factory method is added to the parent
     * predicate class for creating this member's predicate.
     *
     * @return the factory method name of the member predicate
     */
    String getFactoryName();

    /**
     * Accepts a {@link MemberDefinitionVisitor} to visit an implementation of {@link MemberDefinition}.
     * <p>
     * Implementations of {@link MemberDefinition} are responsible for forwarding calls to the respective
     * {@code MemberDefinitionVisitor#visit(T)} method for their implementation.
     * <p>
     * {@link MemberDefinitionVisitor} defines a separate {@code MemberDefinitionVisitor#visit(T)} method for each
     * expected implementation of {@link MemberDefinition}. It is considered an error to create an implementation of
     * {@link MemberDefinition} that is not covered by the visitor or for the {@link MemberDefinition} to do
     * nothing in this method.
     *
     * @param visitor the visitor to accept
     * @param <T>     the return type of the visitation of an implementation class
     * @return the value the visitor produces after visiting an implementation class
     */
    <T> T accept(MemberDefinitionVisitor<T> visitor);
}
