package com.simibubi.create.foundation.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.extensions.IForgeBlock;

public enum ContraptionMovementSetting {
	MOVABLE, NO_PICKUP, UNMOVABLE;

	private static HashMap<ResourceLocation, Supplier<ContraptionMovementSetting>> registry = new HashMap<>();

	public static void register(ResourceLocation id, Supplier<ContraptionMovementSetting> setting) {
		registry.put(id, setting);
	}

	static {
		register(Blocks.BUDDING_AMETHYST.getRegistryName(), () -> AllConfigs.SERVER.kinetics.amethystMovement.get());
		register(Blocks.SPAWNER.getRegistryName(), () -> AllConfigs.SERVER.kinetics.spawnerMovement.get());
		register(Blocks.OBSIDIAN.getRegistryName(), () -> AllConfigs.SERVER.kinetics.obsidianMovement.get());
		register(Blocks.CRYING_OBSIDIAN.getRegistryName(), () -> AllConfigs.SERVER.kinetics.obsidianMovement.get());
	}

	@Nullable
	public static ContraptionMovementSetting get(Block block) {
		if (block instanceof IMovementSettingProvider)
			return ((IMovementSettingProvider) block).getContraptionMovementSetting();
		return get(block.getRegistryName());
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

	public interface IMovementSettingProvider extends IForgeBlock {
		ContraptionMovementSetting getContraptionMovementSetting();
	}
}
