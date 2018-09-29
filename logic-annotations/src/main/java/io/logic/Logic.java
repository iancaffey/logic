package io.logic;

import com.google.gson.TypeAdapterFactory;
import org.immutables.gson.Gson.ExpectedSubtypes;

import javax.annotation.processing.Processor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * A representation of the logic definition of a Java class.
 * <p>
 * {@link Logic} detects value-providing members within the class file to auto-generate predicate implementations. These
 * members are discovered by a combination of patterns and visibility restrictions.
 * <p>
 * A {@link Predicate} is auto-generated for the class file, with the name {@code {Class#getSimpleName()}Predicate}, in
 * the same package as the class file. Three standard predicate implementations are generated for every logic class as
 * inner classes of the generated predicate, ({@code And}, {@code Or}, and {@code Not}).
 * <p>
 * For each field and method within a class, if the member matches any of the patterns, {@link Logic#fields()} for
 * fields or {@link Logic#methods()} for methods, and has an access modifier within the set of visibility restrictions,
 * {@link Logic#fieldVisibility()} or {@link Logic#methodVisibility()} for methods, a predicate implementation is created
 * that tests the result of the value-providing member against a predicate.
 * <p>
 * {@link Mixin} allows for hooking in arbitrary Java predicates into the predicate implementation of the class file.
 *
 * @author Ian Caffey
 * @since 1.0
 */
@Documented
@Target(ElementType.TYPE)
public @interface Logic {
    /**
     * Represents the set of patterns to use when matching against fields of the model class.
     * <p>
     * {@code *} is used to match against the component of the field name that represents the "logic name" for the
     * field (e.g. {@code _*} matching against the field {@code _modelName} would produce the logic name {@code ModelName}).
     * <p>
     * The logic name is relevant for predicate generation as it is used when naming the predicate implementation class.
     *
     * @return the field patterns
     */
    String[] fields() default "*";

    /**
     * Represents the set of patterns to use when matching against methods of the model class.
     * <p>
     * {@code *} is used to match against the component of the method name that represents the "logic name" for the
     * field (e.g. {@code get*} matching against the method {@code getModelName()} would produce the logic name {@code ModelName}).
     * <p>
     * The logic name is relevant for predicate generation as it is used when naming the predicate implementation class.
     *
     * @return the method patterns
     */
    String[] methods() default "get*";

    /**
     * Represents the set of access modifiers to include when discovering fields of the model class.
     * <p>
     * Each non-static field must have an access modifier within the set of visibility, otherwise it is ignored.
     * <ul>
     * <li>{@link Visibility#PUBLIC} would include {@code public} fields.</li>
     * <li>{@link Visibility#PROTECTED} would include {@code protected} fields.</li>
     * <li>{@link Visibility#PACKAGE} would include {@code package-local} (no access modifier) fields.</li>
     * <li>{@link Visibility#PRIVATE} would include nothing as the predicate implementation does not have access to the field.</li>
     * </ul>
     *
     * @return the field visibility restrictions
     */
    Visibility[] fieldVisibility() default Visibility.PUBLIC;

    /**
     * Represents the set of access modifiers to include when discovering methods of the model class.
     * <p>
     * Each non-static, zero-arg, method must have an access modifier within the set of visibility, otherwise it is ignored.
     * <ul>
     * <li>{@link Visibility#PUBLIC} would include {@code public} methods.</li>
     * <li>{@link Visibility#PROTECTED} would include {@code protected} methods.</li>
     * <li>{@link Visibility#PACKAGE} would include {@code package-local} (no access modifier) methods.</li>
     * <li>{@link Visibility#PRIVATE} would include nothing as the predicate implementation does not have access to the method.</li>
     * </ul>
     *
     * @return the field visibility restrictions
     */
    Visibility[] methodVisibility() default Visibility.PUBLIC;

    /**
     * Represents the set of mixins to include as implementations of the model predicate.
     * <p>
     * {@link Mixin} is a declarative representation of a custom model predicate implementation. The mixin defines the
     * set of values of the predicate and the body of the predicate test method.
     *
     * @return the mixins to include when generating the predicates
     */
    Mixin[] mixins() default {};

    /**
     * Represents the package to place the model predicate.
     * <p>
     * {@code ""} represents using the package of the annotation use-site for the model predicate.
     *
     * @return the package of the model predicate
     */
    String namespace() default "";

    /**
     * Represents whether or not to generate all the {@link TypeAdapterFactory} required for serializing
     * the type hierarchy of logic predicates.
     * <p>
     * Generated {@link TypeAdapterFactory} are registered in <b>two places </b> with {@link ServiceLoader}.
     * <ul>
     * <li>{@code io.logic.gson.TypeAdapterFactoryMirror} represents the custom Logic type adapter factories.</li>
     * <li>All other type adapter factories that are auto-generated by Immutables are found by {@link TypeAdapterFactory}</li>
     * </ul>
     * There is no way to get around this limitation as distinct {@link Processor} cannot modify the same service file
     * and the only way to auto-generate type adapter factories for the logic predicates is through a
     * {@code RuntimeTypeAdapter}. {@link ExpectedSubtypes} does not perform well and requires distinct JSON models to
     * distinguish between subtypes and would require providing an obscure discriminator method to the model that serves
     * no purpose but to enable JSON serialization.
     *
     * @return whether or not to generate the type adapter factories for the logic predicates
     */
    boolean gson() default true;

    /**
     * Represents whether or not to implement the <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor pattern</a>
     * for the type hierarchy of logic predicates.
     *
     * @return whether or not to generate the visitor pattern for the logic predicates
     */
    boolean visitor() default true;

    /**
     * An annotation that can be placed on types or packages to designate a set of {@link Class} to include for generating
     * model predicates.
     * <p>
     * {@link Include} allows for generating predicate implementations for arbitrary Java classes, even classes outside
     * your current codebase.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    @Documented
    @Repeatable(Includes.class)
    @Target({ElementType.TYPE, ElementType.PACKAGE})
    @interface Include {
        /**
         * Represents the set of classes to include for predicate generation.
         *
         * @return the classes to include
         */
        Class<?>[] value() default {};

        /**
         * Represents the {@link Logic} definition to use when generating the predicate implementations for the included
         * classes.
         *
         * @return the included class logic definition
         */
        Logic logic() default @Logic;
    }

    /**
     * An annotation that represents the container for repeated {@link Include} annotations.
     * <p>
     * {@link Repeatable} requires a container annotation to place the repeated annotations within during compilation.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    @Documented
    @Target({ElementType.TYPE, ElementType.PACKAGE})
    @interface Includes {
        /**
         * Represents the collection of {@link Include} annotations being repeated.
         *
         * @return the repeated include annotations
         */
        Include[] value();
    }

    /**
     * An annotation that can be placed on value-providing members of {@link Logic} classes to provide an alternate name
     * when generating the predicate implementation.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Named {
        /**
         * Represents the name to use for the value-providing member.
         *
         * @return the name of the member
         */
        String value();
    }

    /**
     * An annotation that can be placed on value-providing members of {@link Logic} classes to ignore them during the
     * discovery phase of the {@link Logic} model class.
     * <p>
     * {@link Ignore} allows for a more granular level of ignoring fields and methods than adjusting the patterns and
     * visibility restrictions of {@link Logic} to exclude the member.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Ignore {
    }

    /**
     * A declarative representation of a custom predicate implementation of the {@link Logic} model class.
     * <p>
     * {@link Mixin} defines the set of values of the predicate and the body of the predicate test method.
     * <p>
     * {@link Mixin#parameters()} are used to create fields within the predicate implementation to allow passing in
     * values to the predicate implementation.
     * <p>
     * {@link Mixin#expression()} and {@link Mixin#arguments()} are used in combination to represent the
     * {@link Predicate#test(Object)} body.
     *
     * <a href="https://github.com/square/javapoet">JavaPoet</a> is used for converting {@link Mixin#expression()} into
     * a code block.
     *
     * @author Ian Caffey
     * @since 1.0
     */
    @Documented
    @Target({})
    @interface Mixin {
        /**
         * Represents the predicate class name of the mixin.
         *
         * @return the class name of the mixin predicate
         */
        String name();

        /**
         * Represents the factory method name for the mixin.
         * <p>
         * A factory method is placed within the parent predicate implementation to provide an easier way of creating
         * the mixin predicate.
         *
         * @return the factory method name of the mixin predicate
         */
        String factoryName();

        /**
         * Represents the parameters to the mixin.
         * <p>
         * Mixin parameters are represented as fields within the mixin predicate implementation.
         *
         * @return the mixin parameters
         */
        Parameter[] parameters() default {};

        /**
         * Represents the single-line Java expression that composes the body of the predicate test method.
         * <p>
         * The format of the expression is expected to be compatible with <a href="https://github.com/square/javapoet">JavaPoet</a>.
         *
         * @return the mixin predicate test method body
         */
        String expression();

        /**
         * Represents the arguments to the expression that composes the body of the predicate test method.
         * <p>
         * The arguments are passed when formatting the expression string into a code block.
         *
         * @return the mixin predicate test method expression arguments
         */
        Argument[] arguments() default {};

        /**
         * A representation of an argument to the <a href="https://github.com/square/javapoet">JavaPoet</a> formatter.
         * <p>
         * {@link Argument} can represent either a {@link String} literal or a {@link Class}.
         * <p>
         * If {@link Argument#value()} is non-empty, it is used as the argument value. Otherwise {@link Argument#type()}
         * is considered the true value of the argument.
         * <p>
         * This design allows for a hacked polymorphic type to allow for proper class handling in the formatted
         * expression to resolve imports.
         *
         * @author Ian Caffey
         * @since 1.0
         */
        @interface Argument {
            /**
             * Represents the string value of the argument.
             * <p>
             * If the value is non-empty, the argument is interpreted as a {@link String}.
             *
             * @return the argument string value
             */
            String value() default "";

            /**
             * Represents the class value of the argument.
             * <p>
             * If the string value is empty, the argument is interpreted as a {@link Class}.
             *
             * @return the argument class value
             */
            Class<?> type() default Void.class;
        }

        /**
         * A representation of a parameter to a {@link Mixin}.
         * <p>
         * {@link Parameter} corresponds to a field in the {@link Mixin} predicate implementation which can be accessed within
         * the code block through the corresponding getter method.
         *
         * @author Ian Caffey
         * @since 1.0
         */
        @Documented
        @Target({})
        @interface Parameter {
            /**
             * Represents the field name of the parameter.
             *
             * @return the parameter field name
             */
            String name();

            /**
             * Represents the type of the parameter.
             *
             * @return the parameter type
             */
            Class<?> type();
        }
    }
}
