package com.simibubi.create.foundation.utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Temporary source-compat annotation retained for package-info files.
 */
@Deprecated(forRemoval = true)
@Nonnull
@TypeQualifierDefault({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface FieldsAreNonnullByDefault {
}
