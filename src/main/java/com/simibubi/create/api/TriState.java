package com.simibubi.create.api;

public enum TriState {
	TRUE,
	DEFAULT,
	FALSE;

	public boolean isTrue() {
		return this == TRUE;
	}

	public boolean isDefault() {
		return this == DEFAULT;
	}

	public boolean isFalse() {
		return this == FALSE;
	}

	public boolean getValue() {
		return switch (this) {
			case TRUE -> true;
			case DEFAULT -> throw new IllegalArgumentException("Default does not have a value");
			case FALSE -> false;
		};
	}
}
