package com.simibubi.create.foundation.advancement;

import java.util.function.Function;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleSerializableTrigger<T> extends StringSerializableTrigger<T> {

	private final Function<T, String> encoder;
	private final Function<String, T> decoder;

	public SimpleSerializableTrigger(String id, Function<T, String> encoder, Function<String, T> decoder) {
		super(id);
		this.encoder = encoder;
		this.decoder = decoder;
	}

	@Nullable
	@Override
	protected T getValue(String key) {
		try {
			return decoder.apply(key);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	@Nullable
	@Override
	protected String getKey(@Nullable T value) {
		if (value == null)
			return null;
		return encoder.apply(value);
	}
}
