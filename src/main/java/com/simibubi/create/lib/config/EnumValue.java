package com.simibubi.create.lib.config;

public class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
	public EnumValue(T value) {
		super(value);
	}
}
