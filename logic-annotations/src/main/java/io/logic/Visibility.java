package io.logic;

/**
 * An enumeration of access level modifiers.
 * <p>
 * Access level modifiers determine whether other classes can use a particular field or invoke a particular method.
 * <p>
 * There are two levels of access control:
 * <ul>
 * <li>At the top level—public, or package-private (no explicit modifier).</li>
 * <li>At the member level—public, private, protected, or package-private (no explicit modifier).</li>
 * </ul>
 *
 * @author Ian Caffey
 * @since 1.0
 */
public enum Visibility {
    PUBLIC, PRIVATE, PACKAGE, PROTECTED
}
