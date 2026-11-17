package com.simibubi.create.foundation.utility;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.createmod.catnip.data.Glob;

public class GlobCache {
	private static final CacheLoader<String, Pattern> LOADER = CacheLoader.from(globPattern -> {
		String regex = Glob.toRegexPattern(globPattern, "");
		return Pattern.compile(regex);
	});
	private static final LoadingCache<String, Pattern> GLOB_CACHE = CacheBuilder.newBuilder()
		.expireAfterAccess(15, TimeUnit.MINUTES)
		.maximumSize(2048)
		.build(LOADER);

	public static Pattern get(String globPattern) {
		return GLOB_CACHE.getUnchecked(globPattern);
	}

	public static Predicate<String> getPredicate(String globPattern) {
		return get(globPattern).asMatchPredicate();
	}
}
