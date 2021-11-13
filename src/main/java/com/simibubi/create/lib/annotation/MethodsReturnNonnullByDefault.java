package com.simibubi.create.lib.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * This annotation can be applied to a package or class to indicate that
 * the methods in that element are nullable by default unless there is:
 * <ul>
 * <li>An explicit nullness annotation
 * <li>The method overrides a method in a superclass (in which case the
 * annotation of the corresponding parameter in the superclass applies)
 * <li>There is a default parameter annotation applied to a more tightly nested element.
 * </ul>
 * <p>This annotation implies the same "nullness" as no annotation. However, it is different
 * than having no annotation, as it is inherited and it can override a {@link MethodsReturnNonnullByDefault}
 * annotation at an outer scope.
 *
 * @see Nullable
 */
@Documented
@Nullable
@TypeQualifierDefault(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodsReturnNonnullByDefault {
}
