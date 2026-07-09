package com.simibubi.create.compat;

import java.util.Optional;
import java.util.function.Supplier;

import net.createmod.catnip.api.lang.Lang;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.fml.loading.LoadingModList;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	AETHER,
	AETHER_II,
	BETTEREND,
	COMPUTERCRAFT,
	CURIOS,
	DYNAMICTREES,
	JEI,
	FUNCTIONALSTORAGE,
	OCCULTISM,
	PACKETFIXER,
	SOPHISTICATEDBACKPACKS,
	SOPHISTICATEDSTORAGE,
	STORAGEDRAWERS,
	TCONSTRUCT,
	FRAMEDBLOCKS,
	XLPACKETS,
	MODERNUI,
	FTBCHUNKS,
	JOURNEYMAP,
	XAEROWORLDMAP,
	FTBLIBRARY,
	SODIUM,
	INVENTORYSORTER,
	FARMERSDELIGHT;

	private final String id;
	private final boolean isLoaded;

	Mods() {
		id = Lang.asId(name());
		isLoaded = LoadingModList.get().getModFileById(id) != null;
	}

	/**
	 * @return the mod id
	 */
	public String id() {
		return id;
	}

	public Identifier rl(String path) {
		return Identifier.fromNamespaceAndPath(id, path);
	}

	public Block getBlock(String id) {
		return BuiltInRegistries.BLOCK.get(rl(id))
			.map(holder -> holder.value())
			.orElse(Blocks.AIR);
	}

	public Item getItem(String id) {
		return BuiltInRegistries.ITEM.get(rl(id))
			.map(holder -> holder.value())
			.orElse(Items.AIR);
	}

	public boolean contains(ItemLike entry) {
		if (!isLoaded())
			return false;
		Item asItem = entry.asItem();
		return asItem != null && RegisteredObjectsHelper.getKeyOrThrow(asItem)
			.getNamespace()
			.equals(id);
	}

	/**
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Simple hook to run code if a mod is installed
	 *
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
	 *
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) {
			toExecute.get().run();
		}
	}
}
