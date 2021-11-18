package com.simibubi.create.lib.config;

import java.util.Locale;

public enum ConfigType {
	COMMON,
	CLIENT,
	SERVER;

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
