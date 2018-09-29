package io.logic;

import io.logic.Logic.Include;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Immutable;

import javax.lang.model.element.Element;

/**
 * A representation of the entire context of the {@link Logic} annotation included for a primitive type.
 * <p>
 * {@link PrimitiveTypeLogic} provides the entire context for a {@link Logic} by providing the originating program element,
 * referenced primitive type to generate a logic model, and the logic model configuration itself.
 * <p>
 * For standard distributions of <a href="https://github.com/iancaffey/logic">logic</a>, clients cannot include logic
 * configurations for primitive types as every primitive type has a predefined logic configuration in the runtime, but
 * clients can use the unpackaged distribution of logic-annotations and logic-processor which lacks a runtime and provide
 * user-defined logic configurations that fit their preference.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface PrimitiveTypeLogic {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutablePrimitiveTypeLogic.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static PrimitiveTypeLogic of(Element source, Class<?> type, LogicSpec logic) {
        return ImmutablePrimitiveTypeLogic.of(source, type, logic);
    }

    /**
     * Represents the program element which was annotated with {@link Include}.
     *
     * @return the source program element for the logic annotation
     */
    Element getSource();

    /**
     * Represents the {@link Class} that corresponds to the primitive type being modeled.
     *
     * @return the class of the primitive model
     */
    Class<?> getType();

    /**
     * Represents the specification of the {@link Logic} annotation used for configuring the logic model.
     *
     * @return the logic model configuration
     */
    LogicSpec getLogic();

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder setSource(Element source);

        Builder setType(Class<?> type);

        Builder setLogic(LogicSpec logic);

        PrimitiveTypeLogic build();
    }
}
