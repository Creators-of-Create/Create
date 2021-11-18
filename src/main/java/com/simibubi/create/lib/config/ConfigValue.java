package com.simibubi.create.lib.config;

public class ConfigValue<T> {
	public T value;

	public ConfigValue(T value) {
        this.value = value;
    }

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}
}
