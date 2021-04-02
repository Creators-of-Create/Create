package com.simibubi.create.foundation;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtil {

	public static <T> Optional<T> thenTry(Optional<T> first, Optional<T> thenTry) {
		if (first.isPresent()) {
			return first;
		} else {
			return thenTry;
		}
	}

	public static <T> Optional<T> thenTryLazy(Supplier<Optional<T>> first, Supplier<Optional<T>> thenTry) {
		Optional<T> one = first.get();
		if (one.isPresent()) {
			return one;
		} else {
			return thenTry.get();
		}
	}
}
