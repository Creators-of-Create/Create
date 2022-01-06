package com.simibubi.create.lib.util;

import java.util.Set;

import com.tterrag.registrate.util.nullness.NonNullConsumer;

public interface ListenerProvider {
	Set<NonNullConsumer<ListenerProvider>> getListeners();

	default void addListener(NonNullConsumer<ListenerProvider> listener) {
		getListeners().add(listener);
	}

	default void invalidate() {
		Set<NonNullConsumer<ListenerProvider>> listeners = getListeners();
		listeners.forEach(listener -> listener.accept(this));
		listeners.clear();
	}
}
