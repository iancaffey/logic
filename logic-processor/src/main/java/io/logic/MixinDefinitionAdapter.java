package io.logic;

import com.squareup.javapoet.CodeBlock;
import io.logic.Logic.Mixin;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * A utility class for converting a {@link Mixin} annotation into a more usable form {@link MixinDefinition}.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@UtilityClass
public class MixinDefinitionAdapter {
    /**
     * Converts the specified {@link Mixin} annotation into a {@link MixinDefinition}.
     * <ul>
     * <li>{@link Mixin#name()} is used as {@link MixinDefinition#getPredicateName()}.</li>
     * <li>{@link Mixin#factoryName()} is used as {@link MixinDefinition#getFactoryName()}.</li>
     * <li>{@link Mixin#parameterNames()} and {@link Mixin#parameterTypes()} are zipped to form
     * {@link MixinDefinition#getParameters()}.</li>
     * <li>{@link Mixin#expression()} and {@link Mixin#arguments()} are formatted into a {@link CodeBlock}. If the return
     * statement is missing from the expression, it is appended to the expression in the code block.</li>
     * </ul>
     *
     * @param mixin the mixin annotation to convert
     * @return a new {@link MixinDefinition} representing the annotation
     */
    public MixinDefinition convert(@NonNull MixinSpec mixin) {
        return MixinDefinition.builder()
                .setPredicateName(mixin.getName())
                .setFactoryName(mixin.getFactoryName())
                .putAllParameters(mixin.getParameters())
                .setBody(mixin.getExpression(), (Object[]) mixin.getArguments().toArray())
                .build();
    }
}
