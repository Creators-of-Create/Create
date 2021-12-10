package com.simibubi.create.foundation.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public enum ContraptionMovementSetting {
	MOVABLE, NO_PICKUP, UNMOVABLE;

	private static HashMap<ResourceLocation, Supplier<ContraptionMovementSetting>> registry = new HashMap<>();

	public static void register(ResourceLocation id, Supplier<ContraptionMovementSetting> setting) {
		registry.put(id, setting);
	}

	static {
		register(Registry.BLOCK.getKey(Blocks.BUDDING_AMETHYST), () -> AllConfigs.SERVER.kinetics.amethystMovement.get());
		register(Registry.BLOCK.getKey(Blocks.SPAWNER), () -> AllConfigs.SERVER.kinetics.spawnerMovement.get());
		register(Registry.BLOCK.getKey(Blocks.OBSIDIAN), () -> AllConfigs.SERVER.kinetics.obsidianMovement.get());
		register(Registry.BLOCK.getKey(Blocks.CRYING_OBSIDIAN), () -> AllConfigs.SERVER.kinetics.obsidianMovement.get());
	}

	@Nullable
	public static ContraptionMovementSetting get(Block block) {
		if (block instanceof IMovementSettingProvider)
			return ((IMovementSettingProvider) block).getContraptionMovementSetting();
		return get(Registry.BLOCK.getKey(block));
	}

	@Nullable
	public static ContraptionMovementSetting get(ResourceLocation id) {
		Supplier<ContraptionMovementSetting> supplier = registry.get(id);
		return supplier == null ? null : supplier.get();
	}

	protected static boolean allAre(Collection<StructureTemplate.StructureBlockInfo> blocks, ContraptionMovementSetting are) {
		return blocks.stream().anyMatch(b -> get(b.state.getBlock()) == are);
	}

	public static boolean isNoPickup(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return allAre(blocks, ContraptionMovementSetting.NO_PICKUP);
	}

	public interface IMovementSettingProvider /* extends Block */ {
		ContraptionMovementSetting getContraptionMovementSetting();
	}
}
