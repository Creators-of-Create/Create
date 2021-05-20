package com.simibubi.create.compat;

import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	DYNAMICTREES;

	/**
	 * @return a boolean of whether the mod is loaded or not
	 */
	public boolean isLoaded() {
		return ModList.get().isLoaded(asId());
	}

	public String asId() {
		return name().toLowerCase();
	}

	/**
	 * Simple hook to run code if a mod is installed
	 * @param toRun will be run if the mod is loaded
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded())
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}
}
