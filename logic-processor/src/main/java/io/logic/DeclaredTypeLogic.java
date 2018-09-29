package io.logic;

import io.logic.Logic.Include;
import io.logic.immutables.ImmutableLogicStyle;
import org.immutables.value.Value.Immutable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * A representation of the entire context of the {@link Logic} annotation placed on a declared type.
 * <p>
 * {@link DeclaredTypeLogic} provides the entire context for a {@link Logic} declaration by providing the originating
 * program element, referenced type to generate a logic model, and the logic model configuration itself.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Immutable
@ImmutableLogicStyle
public interface DeclaredTypeLogic {
    //Immutables builder stub to hide immutable class dependency
    static Builder builder() {
        return ImmutableDeclaredTypeLogic.builder();
    }

    //Immutables factory stub to hide immutable class dependency
    static DeclaredTypeLogic of(Element source, TypeElement type, LogicSpec logic) {
        return ImmutableDeclaredTypeLogic.of(source, type, logic);
    }

    /**
     * Represents the program element which was annotated with {@link Logic} or {@link Include}.
     * <p>
     * For declared types that were directly annotated with {@link Logic}, {@code getSource() == getType()}.
     *
     * @return the source program element for the logic annotation
     */
    Element getSource();

    /**
     * Represents the program element which corresponds to the type having a logic model generated.
     *
     * @return the type being modeled
     */
    TypeElement getType();

    /**
     * Represents the specification of the {@link Logic} annotation used for configuring the logic model.
     *
     * @return the logic model configuration
     */
    LogicSpec getLogic();

    //Immutables builder stub to hide immutable class dependency
    interface Builder {
        Builder setSource(Element element);

        Builder setType(TypeElement type);

        Builder setLogic(LogicSpec logic);

        DeclaredTypeLogic build();
    }
}
