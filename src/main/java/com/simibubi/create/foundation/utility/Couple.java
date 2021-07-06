package com.simibubi.create.foundation.utility;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class Couple<T> extends Pair<T, T> implements Iterable<T> {

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

	public <S> Couple<S> mapWithContext(BiFunction<T, Boolean, S> function) {
		return Couple.create(function.apply(first, true), function.apply(second, false));
	}

	public <S, R> Couple<S> mapWithParams(BiFunction<T, R, S> function, Couple<R> values) {
		return Couple.create(function.apply(first, values.first), function.apply(second, values.second));
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

	@Override
	public void forEach(Consumer<? super T> consumer) {
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

	@Override
	public Iterator<T> iterator() {
		return new Couplerator<>(this);
	}

	public Stream<T> stream() {
		return Stream.of(first, second);
	}

	private static class Couplerator<T> implements Iterator<T> {

		int state;
		private Couple<T> couple;

		public Couplerator(Couple<T> couple) {
			this.couple = couple;
			state = 0;
		}

		@Override
		public boolean hasNext() {
			return state != 2;
		}

		@Override
		public T next() {
			state++;
			if (state == 1)
				return couple.first;
			if (state == 2)
				return couple.second;
			return null;
		}

	}

}
