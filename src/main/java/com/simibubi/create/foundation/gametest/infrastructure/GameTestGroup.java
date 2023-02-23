package com.simibubi.create.foundation.gametest.infrastructure;

import com.simibubi.create.Create;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GameTestGroup {
	String path();
	String namespace() default Create.ID;
}
