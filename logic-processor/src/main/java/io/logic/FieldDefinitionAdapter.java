package io.logic;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.TypeName;
import io.logic.Logic.Named;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.lang.model.element.VariableElement;

/**
 * A utility class for converting a {@link VariableElement} program element to a {@link FieldDefinition}.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@UtilityClass
public class FieldDefinitionAdapter {
    /**
     * Converts the specified {@link VariableElement} into a {@link FieldDefinition} using the specified name as the
     * predicate name, unless the field is annotated with {@link Named} where that value is used in replace of the provided
     * name.
     *
     * @param name  the predicate name for the field definition
     * @param field the field program element which is being defined
     * @return a new {@link FieldDefinition} for the field
     */
    public FieldDefinition convert(@NonNull String name, @NonNull VariableElement field) {
        Named named = field.getAnnotation(Named.class);
        return FieldDefinition.builder()
                .setName(field.getSimpleName().toString())
                .setType(TypeName.get(field.asType()))
                .setPredicateName(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, named != null ? named.value() : name))
                .build();
    }
}
