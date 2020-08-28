package com.simibubi.create.foundation.utility;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class Couple<T> extends Pair<T, T> {

	private static Couple<Boolean> TRUE_AND_FALSE = Couple.create(true, false);

	protected Couple(T first, T second) {
		super(first, second);
	}

	public static <T> Couple<T> create(T first, T second) {
		return new Couple<>(first, second);
	}

	public static <T> Couple<T> create(Supplier<T> factory) {
		return new Couple<>(factory.get(), factory.get());
	}

	public T get(boolean first) {
		return first ? getFirst() : getSecond();
	}

	public void set(boolean first, T value) {
		if (first)
			setFirst(value);
		else
			setSecond(value);
	}

	@Override
	public Couple<T> copy() {
		return create(first, second);
	}

	public <S> Couple<S> map(Function<T, S> function) {
		return Couple.create(function.apply(first), function.apply(second));
	}

	public void replace(Function<T, T> function) {
		setFirst(function.apply(getFirst()));
		setSecond(function.apply(getSecond()));
	}

	public void replaceWithContext(BiFunction<T, Boolean, T> function) {
		replaceWithParams(function, TRUE_AND_FALSE);
	}

	public <S> void replaceWithParams(BiFunction<T, S, T> function, Couple<S> values) {
		setFirst(function.apply(getFirst(), values.getFirst()));
		setSecond(function.apply(getSecond(), values.getSecond()));
	}

	public void forEach(Consumer<T> consumer) {
		consumer.accept(getFirst());
		consumer.accept(getSecond());
	}

	public void forEachWithContext(BiConsumer<T, Boolean> consumer) {
		forEachWithParams(consumer, TRUE_AND_FALSE);
	}

	public <S> void forEachWithParams(BiConsumer<T, S> function, Couple<S> values) {
		function.accept(getFirst(), values.getFirst());
		function.accept(getSecond(), values.getSecond());
	}

	public Couple<T> swap() {
		return Couple.create(second, first);
	}

	public ListNBT serializeEach(Function<T, CompoundNBT> serializer) {
		return NBTHelper.writeCompoundList(ImmutableList.of(first, second), serializer);
	}

	public static <S> Couple<S> deserializeEach(ListNBT list, Function<CompoundNBT, S> deserializer) {
		List<S> readCompoundList = NBTHelper.readCompoundList(list, deserializer);
		return new Couple<>(readCompoundList.get(0), readCompoundList.get(1));
	}

}
