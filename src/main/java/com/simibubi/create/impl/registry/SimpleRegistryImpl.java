package com.simibubi.create.impl.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.foundation.mixin.accessor.StateHolderAccessor;
import com.simibubi.create.impl.registry.SimpleRegistryImpl.MultiImpl;
import com.simibubi.create.impl.registry.SimpleRegistryImpl.SingleImpl;

import net.minecraft.world.level.block.state.StateHolder;

// methods are synchronized since registrations can happen during parallel mod loading
public abstract sealed class SimpleRegistryImpl<K, V> implements SimpleRegistry<K, V> permits SingleImpl, MultiImpl {
	protected final Map<K, V> registrations = new IdentityHashMap<>();
	protected final List<Provider<K, V>> providers = new ArrayList<>();

	@Override
	public synchronized void register(K object, V value) {
		Objects.requireNonNull(object, "object");
		Objects.requireNonNull(value, "value");

		V existing = this.registrations.get(object);
		if (existing != null) {
			throw new IllegalArgumentException(String.format(
				"Tried to register duplicate values for object %s (%s): old=%s, new=%s",
				object, object.getClass(), existing, value
			));
		}

		this.registrations.put(object, value);
	}

	@Override
	public synchronized void registerProvider(Provider<K, V> provider) {
		Objects.requireNonNull(provider);
		if (this.providers.contains(provider)) {
			throw new IllegalArgumentException("Tried to register provider twice: " + provider);
		}

		// add to start of list so it's queried first
		this.providers.add(0, provider);
		provider.onRegister(this::invalidate);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public synchronized V get(StateHolder<K, ?> state) {
		Objects.requireNonNull(state, "state");
		K owner = ((StateHolderAccessor<K, ?>) state).getOwner();
		return this.get(owner);
	}

	public static <K, V> SimpleRegistry<K, V> single() {
		return new SingleImpl<>();
	}

	public static <K, V> SimpleRegistry.Multi<K, V> multi() {
		return new MultiImpl<>();
	}

	static final class SingleImpl<K, V> extends SimpleRegistryImpl<K, V> {
		private static final Object nullMarker = new Object();

		private final Map<K, V> providedValues = new IdentityHashMap<>();

		@Override
		@Nullable
		public synchronized V get(K object) {
			Objects.requireNonNull(object, "object");
			if (this.registrations.containsKey(object)) {
				return this.registrations.get(object);
			} else if (this.providedValues.containsKey(object)) {
				V provided = this.providedValues.get(object);
				return provided == nullMarker ? null : provided;
			}

			// no value known, check providers
			// new providers are added to the start, so normal iteration is reverse-registration order
			for (Provider<K, V> provider : this.providers) {
				V value = provider.get(object);
				if (value != null) {
					this.providedValues.put(object, value);
					return value;
				}
			}

			// no provider returned non-null
			this.providedValues.put(object, nullMarker());
			return null;
		}

		@Override
		public void invalidate() {
			this.providedValues.clear();
		}

		@SuppressWarnings("unchecked")
		private static <T> T nullMarker() {
			return (T) nullMarker;
		}
	}

	static final class MultiImpl<K, V> extends SimpleRegistryImpl<K, List<V>> implements SimpleRegistry.Multi<K, V> {
		private final Map<K, List<V>> totals = new IdentityHashMap<>();

		@Override
		public synchronized void add(K object, V value) {
			Objects.requireNonNull(object, "object");
			Objects.requireNonNull(value, "value");

			if (!this.registrations.containsKey(object)) {
				this.registrations.put(object, new ArrayList<>());
			}

			this.registrations.get(object).add(value);
		}

		@Override
		public void addProvider(Provider<K, V> provider) {
			this.registerProvider(new ProviderWrapper<>(provider));
		}

		@Override
		public synchronized void invalidate() {
			this.totals.clear();
		}

		@Override
		@NotNull
		public synchronized List<V> get(K object) {
			Objects.requireNonNull(object, "object");
			if (!this.totals.containsKey(object)) {
				this.totals.put(object, this.calculateTotal(object));
			}

			return this.totals.get(object);
		}

		private List<V> calculateTotal(K object) {
			List<V> registrations = this.registrations.getOrDefault(object, List.of());
			List<V> total = new ArrayList<>(registrations);

			for (Provider<K, List<V>> provider : this.providers) {
				List<V> values = provider.get(object);
				if (values != null) {
					total.addAll(values);
				}
			}

			return total.isEmpty() ? List.of() : Collections.unmodifiableList(total);
		}

		// remove nullable
		@Override
		public synchronized List<V> get(StateHolder<K, ?> state) {
			return super.get(state);
		}

		private record ProviderWrapper<K, V>(Provider<K, V> wrapped) implements Provider<K, List<V>> {
			@Override
			public List<V> get(K object) {
				V value = this.wrapped.get(object);
				return value == null ? null : List.of(value);
			}

			@Override
			public void onRegister(Runnable invalidate) {
				this.wrapped.onRegister(invalidate);
			}
		}
	}
}
