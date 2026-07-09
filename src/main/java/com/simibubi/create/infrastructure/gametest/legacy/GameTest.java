package com.simibubi.create.infrastructure.gametest.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Temporary Create 26.2 bridge for legacy GameTest declarations.
 * TODO 26.2: Migrate tests to GeneratedTest/FunctionGameTestInstance registration.
 */
@Deprecated(forRemoval = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
	String batch() default "defaultBatch";

	String template() default "";

	int rotationSteps() default 0;

	int timeoutTicks() default 100;

	long setupTicks() default 0L;

	boolean required() default true;

	int attempts() default 1;

	int requiredSuccesses() default 1;
}
