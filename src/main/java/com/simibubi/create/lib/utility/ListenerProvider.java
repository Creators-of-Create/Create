package com.simibubi.create.lib.utility;

import java.util.Set;

import com.tterrag.registrate.util.nullness.NonNullConsumer;

public interface ListenerProvider {
	Set<NonNullConsumer> getListeners();
	default void addListener(NonNullConsumer listener) {
		getListeners().add(listener);
	}

	default void invalidate() {
		getListeners().forEach(listener -> listener.accept(this));
	}
}
