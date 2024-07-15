package com.simibubi.create.compat;

import java.util.Optional;
import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	COMPUTERCRAFT,
	CONNECTIVITY,
	CURIOS,
	DYNAMICTREES,
	FUNCTIONALSTORAGE,
	OCCULTISM,
	PACKETFIXER,
	SOPHISTICATEDBACKPACKS,
	SOPHISTICATEDSTORAGE,
	STORAGEDRAWERS,
	TCONSTRUCT,
	XLPACKETS;

	private final String id;

	Mods() {
		id = Lang.asId(name());
	}

	/**
	 * @return the mod id
	 */
	public String id() {
		return id;
	}

	public ResourceLocation rl(String path) {
		return new ResourceLocation(id, path);
	}

	public Block getBlock(String id) {
		return ForgeRegistries.BLOCKS.getValue(rl(id));
	}

	/**
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return ModList.get().isLoaded(id);
	}

	/**
	 * Simple hook to run code if a mod is installed
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded())
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) {
			toExecute.get().run();
		}
	}
}
