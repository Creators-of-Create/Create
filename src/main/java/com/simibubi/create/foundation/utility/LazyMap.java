package com.simibubi.create.foundation.utility;

import net.minecraftforge.common.util.NonNullFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LazyMap<K, V> implements Function<K, V> {
	private final NonNullFunction<K, V> function;
	private final Map<K, V> map;

	public LazyMap(NonNullFunction<K, V> function, Map<K, V> map) {
		this.function = function;
		this.map = map;
	}

	public LazyMap(NonNullFunction<K, V> function) {
		this(function, new HashMap<>());
	}

	@Override
	public V apply(K k) {
		return map.computeIfAbsent(k, function::apply);
	}

	public static <K, V> LazyMap<K, V> of(NonNullFunction<K, V> function) {
		return new LazyMap<>(function);
	}
}
