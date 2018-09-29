package io.logic;

import com.squareup.javapoet.TypeName;
import io.logic.Logic.Mixin;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.Map;

/**
 * A representation of the {@link Mixin} annotation that handles the annotation processor restriction of not being able
 * to access {@link Class} fields within the annotation for classes currently being compiled.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface MixinSpec {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutableMixinSpec.builder();
    }

    /**
     * Represents the predicate class name of the mixin.
     *
     * @return the class name of the mixin predicate
     */
    String getName();

    /**
     * Represents the factory method name for the mixin.
     * <p>
     * A factory method is placed within the parent predicate implementation to provide an easier way of creating
     * the mixin predicate.
     *
     * @return the factory method name of the mixin predicate
     */
    String getFactoryName();

    /**
     * Represents the parameters to the mixin.
     * <p>
     * Mixin parameters are represented as fields within the mixin predicate implementation.
     *
     * @return the mixin parameters
     */
    Map<String, TypeName> getParameters();

    /**
     * Represents the single-line Java expression that composes the body of the predicate test method.
     * <p>
     * The format of the expression is expected to be compatible with <a href="https://github.com/square/javapoet">JavaPoet</a>.
     *
     * @return the mixin predicate test method body
     */
    String getExpression();

    /**
     * Represents the arguments to the expression that composes the body of the predicate test method.
     * <p>
     * The arguments are passed when formatting the expression string into a code block.
     *
     * @return the mixin predicate test method expression arguments
     */
    List<Object> getArguments();

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder setName(String name);

        Builder setFactoryName(String factoryName);

        Builder putParameter(String parameterName, TypeName typeName);

        Builder putParameter(Map.Entry<String, ? extends TypeName> entry);

        Builder setParameters(Map<String, ? extends TypeName> entries);

        Builder putAllParameters(Map<String, ? extends TypeName> entries);

        Builder setExpression(String expression);

        Builder addArgument(Object argument);

        Builder addArguments(Object... arguments);

        Builder setArguments(Iterable<? extends Object> arguments);

        Builder addAllArguments(Iterable<? extends Object> arguments);

        MixinSpec build();
    }
}
