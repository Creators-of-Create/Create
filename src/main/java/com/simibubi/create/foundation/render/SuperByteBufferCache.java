package com.simibubi.create.foundation.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SuperByteBufferCache {

	protected final Map<Compartment<?>, Cache<Object, SuperByteBuffer>> caches = new HashMap<>();

	public synchronized void registerCompartment(Compartment<?> compartment) {
		caches.put(compartment, CacheBuilder.newBuilder()
			.build());
	}

	public synchronized void registerCompartment(Compartment<?> compartment, long ticksUntilExpired) {
		caches.put(compartment, CacheBuilder.newBuilder()
			.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
			.build());
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Callable<SuperByteBuffer> callable) {
		Cache<Object, SuperByteBuffer> cache = caches.get(compartment);
		if (cache != null) {
			try {
				return cache.get(key, callable);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public <T> void invalidate(Compartment<T> compartment, T key) {
		caches.get(compartment).invalidate(key);
	}

	public <T> void invalidate(Compartment<?> compartment) {
		caches.get(compartment).invalidateAll();
	}

	public void invalidate() {
		caches.forEach((compartment, cache) -> cache.invalidateAll());
	}

	public static class Compartment<T> {
	}

}
