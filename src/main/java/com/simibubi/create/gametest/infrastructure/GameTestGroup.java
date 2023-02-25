package com.simibubi.create.gametest.infrastructure;

import com.simibubi.create.Create;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows for test method declarations to be concise by moving subdirectories and namespaces to the class level.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTestGroup {
	/**
	 * The subdirectory to search for test structures in.
	 */
	String path();

	/**
	 * The namespace to search for test structures in.
	 */
	String namespace() default Create.ID;
}
