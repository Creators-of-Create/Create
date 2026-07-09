package com.simibubi.create.infrastructure.gametest.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Temporary Create 26.2 bridge for legacy GameTest factories.
 * TODO 26.2: Replace with the 26.2 GameTest loader/instance API.
 */
@Deprecated(forRemoval = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTestGenerator {
}
