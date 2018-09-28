package io.logic;

import com.squareup.javapoet.CodeBlock;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * A representation of the definition of a mixin in a {@link PredicateDefinition}.
 * <p>
 * {@link MixinDefinition} provides the parameters and code block for the mixin predicate along with naming information
 * for generating the predicate implementation for the member.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface MixinDefinition extends MemberDefinition {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutableMixinDefinition.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static MixinDefinition of(String name, String factoryName, Map<String, ? extends Class<?>> entries, CodeBlock body) {
        return ImmutableMixinDefinition.of(name, factoryName, entries, body);
    }

    /**
     * Represents the predicate name of the mixin.
     * <p>
     * When building predicate implementations, this value is used as the class name of the nested class that represents
     * the predicate implementation for this mixin.
     *
     * @return the class name of the mixin predicate
     */
    @Override
    String getPredicateName();

    /**
     * Represents the factory name of the mixin predicate.
     * <p>
     * After building the predicate implementation for this member, a static factory method is added to the parent
     * predicate class for creating this mixin's predicate.
     *
     * @return the factory method name of the mixin predicate
     */
    @Override
    String getFactoryName();

    /**
     * Represents the parameters to the mixin predicate which can be referenced within {@link MixinDefinition#getBody()}
     * that represents the predicate test logic.
     *
     * @return the parameters to the mixin predicate
     */
    Map<String, Class<?>> getParameters();

    /**
     * Represents the body of the mixin predicate test method which returns a boolean when passed in an instance of the
     * predicate model.
     *
     * @return the mixin predicate test method body
     */
    CodeBlock getBody();

    /**
     * Accepts a {@link MemberDefinitionVisitor} to visit an implementation of {@link MemberDefinition}.
     * <p>
     * {@link MemberDefinitionVisitor} is forwarded to {@link MemberDefinitionVisitor#visit(MixinDefinition)}
     * and the result of invoking the visit method is returned.
     *
     * @param visitor the visitor to accept
     * @param <T>     the return type of the visitation of a mixin definition
     * @return the value the visitor produces after visiting a mixin definition
     */
    @Override
    default <T> T accept(MemberDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder setPredicateName(String name);

        Builder setFactoryName(String factoryName);

        Builder putParameter(String name, Class<?> type);

        Builder putParameter(Map.Entry<String, ? extends Class<?>> entry);

        Builder setParameters(Map<String, ? extends Class<?>> entries);

        Builder putAllParameters(Map<String, ? extends Class<?>> entries);

        Builder setBody(CodeBlock body);

        default Builder setBody(String format, Object... args) {
            return setBody(CodeBlock.of(format.startsWith("return") ? format : "return " + format, args));
        }

        MixinDefinition build();
    }
}
