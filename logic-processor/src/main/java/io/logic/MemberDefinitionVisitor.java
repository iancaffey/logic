package io.logic;

/**
 * A representation of a <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor</a> to the
 * {@link MemberDefinition} class hierarchy.
 * <p>
 * Java does not support <a href="https://en.wikipedia.org/wiki/Double_dispatch">double dispatch</a> natively so an
 * interface with knowledge of all implementations of {@link MemberDefinition} is provided with overloaded
 * implementations of {@code MemberDefinitionVisitor#visit(T)}to invoke for each different implementation of
 * {@link MemberDefinition} while the implementation handles the dispatching to the appropriate method call through
 * {@link MemberDefinition#accept(MemberDefinitionVisitor)}.
 *
 * @param <T> the type of value returned from visiting an implementation class
 * @author Ian Caffey
 * @since 1.0
 */
public interface MemberDefinitionVisitor<T> {
    /**
     * Visits the {@link FieldDefinition} implementation of {@link MemberDefinition}.
     * <p>
     * {@link FieldDefinition} will forward to this method in {@link FieldDefinition#accept(MemberDefinitionVisitor)}.
     *
     * @param definition the definition to visit
     * @return the value after visiting the definition
     */
    T visit(FieldDefinition definition);

    /**
     * Visits the {@link MethodDefinition} implementation of {@link MemberDefinition}.
     * <p>
     * {@link MethodDefinition} will forward to this method in {@link MethodDefinition#accept(MemberDefinitionVisitor)}.
     *
     * @param definition the definition to visit
     * @return the value after visiting the definition
     */
    T visit(MethodDefinition definition);

    /**
     * Visits the {@link MixinDefinition} implementation of {@link MemberDefinition}.
     * <p>
     * {@link MixinDefinition} will forward to this method in {@link MixinDefinition#accept(MemberDefinitionVisitor)}.
     *
     * @param definition the definition to visit
     * @return the value after visiting the definition
     */
    T visit(MixinDefinition definition);
}
