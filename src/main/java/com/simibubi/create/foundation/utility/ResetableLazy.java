package com.simibubi.create.foundation.utility;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ResetableLazy<T> implements Supplier<T> {

	private final Supplier<@NotNull T> supplier;
	private T value;

	public ResetableLazy(Supplier<@NotNull T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T get() {
		if (value == null) {
			value = supplier.get();
		}
		return value;
	}

	public void reset() {
		value = null;
	}

	public static <T> ResetableLazy<T> of(Supplier<@NotNull T> supplier) {
		return new ResetableLazy<>(supplier);
	}

}
