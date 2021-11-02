package com.simibubi.create.foundation.advancement;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnumTrigger<T extends Enum<T>> extends StringSerializableTrigger<T> {
	private final Class<T> reference;

	public EnumTrigger(String id, Class<T> reference) {
		super(id);
		this.reference = reference;
	}

	@Nullable
	@Override
	protected T getValue(String key) {
		try {
			return Enum.valueOf(reference, key);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	@Nullable
	@Override
	protected String getKey(@Nullable T value) {
		if (value == null)
			return null;
		return value.name();
	}
}
