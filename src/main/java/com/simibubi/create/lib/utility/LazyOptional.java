package com.simibubi.create.lib.utility;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.MethodsReturnNonnullByDefault;

/**
 * This is pretty much the Forge class copied over, theres really not much change you can do while keeping things functioning.
 * Made some things public and added {@link #getValueUnsafer()} to allow for Bad Ideas™
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LazyOptional<T> {
	public static final @Nonnull LazyOptional<Void> EMPTY = new LazyOptional<>(null);
	private static final Logger LOGGER = LogManager.getLogger();
	public NonNullSupplier<T> supplier;
	private final Object lock = new Object();
	// null -> not resolved yet
	// non-null and contains non-null value -> resolved
	// non-null and contains null -> resolved, but supplier returned null (contract violation)
	private Mutable<T> resolved;
	private final Set<NonNullConsumer<LazyOptional<T>>> listeners = new HashSet<>();
	private boolean isValid = true;

	private LazyOptional(@Nullable NonNullSupplier<T> instanceSupplier) {
		this.supplier = instanceSupplier;
	}

	public static <T> LazyOptional<T> of(final @Nullable NonNullSupplier<T> instanceSupplier) {
		return instanceSupplier == null ? empty() : new LazyOptional<>(instanceSupplier);
	}

	public static <T> LazyOptional<T> ofObject(T o) {
		return LazyOptional.of(() -> o);
	}

	public static <T> LazyOptional<T> empty() {
		return EMPTY.cast();
	}

	@SuppressWarnings("unchecked")
	public <X> LazyOptional<X> cast() {
		return (LazyOptional<X>) this;
	}

	private @Nullable T getValue() {
		if (!isValid || supplier == null) {
			return null;
		}
		if (resolved == null) {
			synchronized (lock) {
				if (resolved == null) {
					T temp = supplier.get();
					if (temp == null) {
						LOGGER.catching(Level.WARN, new NullPointerException("Supplier should not return null value"));
					}
					resolved = new MutableObject<>(temp);
				}
			}
		}
		return resolved.getValue();
	}

	private T getValueUnsafe() {
		T ret = getValue();
		if (ret == null) {
			throw new IllegalStateException("LazyOptional is empty or otherwise returned null from getValue() unexpectedly");
		}
		return ret;
	}

	/**
	 * Not recommended.
	 */
	public T getValueUnsafer() {
		return supplier.get();
	}

	public boolean isPresent() {
		return supplier != null && isValid;
	}

	public void ifPresent(NonNullConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
		T val = getValue();
		if (isValid && val != null)
			consumer.accept(val);
	}

	public <U> LazyOptional<U> lazyMap(NonNullFunction<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		return isPresent() ? of(() -> mapper.apply(getValueUnsafe())) : empty();
	}

	public <U> Optional<U> map(NonNullFunction<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		return isPresent() ? Optional.of(mapper.apply(getValueUnsafe())) : Optional.empty();
	}

	public Optional<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		final T value = getValue();
		return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
	}

	public Optional<T> resolve() {
		return isPresent() ? Optional.of(getValueUnsafe()) : Optional.empty();
	}

	public T orElse(T other) {
		T val = getValue();
		return val != null ? val : other;
	}

	public T orElseGet(NonNullSupplier<? extends T> other) {
		T val = getValue();
		return val != null ? val : other.get();
	}

	public <X extends Throwable> T orElseThrow(NonNullSupplier<? extends X> exceptionSupplier) throws X {
		T val = getValue();
		if (val != null)
			return val;
		throw exceptionSupplier.get();
	}

	public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
		if (isPresent()) {
			this.listeners.add(listener);
		} else {
			listener.accept(this);
		}
	}

	public void invalidate() {
		if (this.isValid) {
			this.isValid = false;
			this.listeners.forEach(e -> e.accept(this));
		}
	}
}
