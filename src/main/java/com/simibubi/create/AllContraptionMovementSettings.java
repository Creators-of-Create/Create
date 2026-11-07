package com.simibubi.create;

import com.simibubi.create.api.contraption.ContraptionMovementSetting;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.world.level.block.Blocks;

public class AllContraptionMovementSettings {
	public static void registerDefaults() {
		ContraptionMovementSetting.REGISTRY.register(Blocks.SPAWNER, () -> AllConfigs.server().kinetics.spawnerMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.TRIAL_SPAWNER, () -> AllConfigs.server().kinetics.spawnerMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.BUDDING_AMETHYST, () -> AllConfigs.server().kinetics.amethystMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.CRYING_OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.RESPAWN_ANCHOR, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		ContraptionMovementSetting.REGISTRY.register(Blocks.REINFORCED_DEEPSLATE, () -> AllConfigs.server().kinetics.reinforcedDeepslateMovement.get());
	}
}
