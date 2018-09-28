package io.logic;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import io.logic.Logic.Named;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.lang.model.element.ExecutableElement;

/**
 * A utility class for converting a {@link ExecutableElement} program element to a {@link MethodDefinition}.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@UtilityClass
public class MethodDefinitionAdapter {
    /**
     * Converts the specified {@link ExecutableElement} into a {@link MethodDefinition} using the specified name as the
     * predicate name, unless the method is annotated with {@link Named} where that value is used in replace of the
     * provided name.
     *
     * @param name   the predicate name for the method definition
     * @param method the method program element which is being defined
     * @return a new {@link MethodDefinition} for the method
     */
    public MethodDefinition convert(@NonNull String name, @NonNull ExecutableElement method) {
        Named named = method.getAnnotation(Named.class);
        return MethodDefinition.builder()
                .setName(method.getSimpleName().toString())
                .setReturnType(ClassName.get(method.getReturnType()))
                .setPredicateName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, named != null ? named.value() : name))
                .build();
    }
}
