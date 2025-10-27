package com.simibubi.create.foundation;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

public interface ICapabilityProvider<T> {
	@Nullable
	T getCapability();

	static <T, C> ICapabilityProvider<T> of(BlockCapabilityCache<T, C> cache) {
		return new BlockCapabilityCacheProvider<>(cache);
	}

	static <T> ICapabilityProvider<T> of(Supplier<T> supplier) {
		return new SupplierProvider<>(supplier);
	}

	static <T> ICapabilityProvider<T> of(T cap) {
		return new SimpleProvider<>(cap);
	}

	@ApiStatus.Internal
	class BlockCapabilityCacheProvider<T, C> implements ICapabilityProvider<T> {
		private final BlockCapabilityCache<T, C> inner;

		private BlockCapabilityCacheProvider(BlockCapabilityCache<T, C> inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner == null ? null : inner.getCapability();
		}
	}

	class SupplierProvider<T> implements ICapabilityProvider<T> {
		private final Supplier<T> inner;

		private SupplierProvider(Supplier<T> inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner == null ? null : inner.get();
		}
	}

	@ApiStatus.Internal
	class SimpleProvider<T> implements ICapabilityProvider<T> {
		private final T inner;

		private SimpleProvider(T inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner;
		}
	}
}
