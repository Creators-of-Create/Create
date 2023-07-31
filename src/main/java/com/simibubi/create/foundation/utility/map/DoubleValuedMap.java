package com.simibubi.create.foundation.utility.map;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.util.TriConsumer;

import com.simibubi.create.foundation.utility.Pair;

public interface DoubleValuedMap<K, V1, V2> extends Map<K, Pair<V1, V2>> {
	default void forEach(TriConsumer<K, V1, V2> action) {
		Map.super.forEach((k, v1V2Pair) -> action.accept(k, v1V2Pair.getFirst(), v1V2Pair.getSecond()));
	}

	@Nullable
	default Pair<V1, V2> put(K key, V1 value1, V2 value2) {
		return put(key, Pair.of(value1, value2));
	}
}
