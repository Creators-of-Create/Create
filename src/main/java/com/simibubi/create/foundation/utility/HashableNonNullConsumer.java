package com.simibubi.create.foundation.utility;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraftforge.common.util.NonNullConsumer;

public class HashableNonNullConsumer<T, H> implements NonNullConsumer<T> {
	private final NonNullConsumer<T> consumer;
	private final H hashKey;

	public HashableNonNullConsumer(NonNullConsumer<T> consumer, H hashKey) {
		this.consumer = consumer;
		this.hashKey = hashKey;
	}

	@Override
	public void accept(@Nonnull T t) {
		consumer.accept(t);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HashableNonNullConsumer<?, ?> that = (HashableNonNullConsumer<?, ?>) o;
		return Objects.equals(hashKey, that.hashKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hashKey);
	}
}
