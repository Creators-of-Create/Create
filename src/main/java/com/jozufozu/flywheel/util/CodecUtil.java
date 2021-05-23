package com.jozufozu.flywheel.util;

import java.util.Collections;
import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

public class CodecUtil {

	/**
	 * Creates a list codec that can be parsed from either a single element or a complete list.
	 */
	public static <T> Codec<List<T>> oneOrMore(Codec<T> codec) {
		return Codec.either(codec.listOf(), codec)
				.xmap(
						either -> either.map(l -> l, Collections::singletonList),
						list -> {
							if (list.size() == 1) {
								return Either.right(list.get(0));
							} else {
								return Either.left(list);
							}
						});
	}
}
