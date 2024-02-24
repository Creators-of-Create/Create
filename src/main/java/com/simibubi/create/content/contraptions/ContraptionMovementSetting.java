package com.simibubi.create.content.contraptions;

import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.registries.ForgeRegistries;

public enum ContraptionMovementSetting {
	MOVABLE, NO_PICKUP, UNMOVABLE;

	private static final AttachedRegistry<Block, Supplier<ContraptionMovementSetting>> SETTING_SUPPLIERS = new AttachedRegistry<>(ForgeRegistries.BLOCKS);

	public static void register(ResourceLocation block, Supplier<ContraptionMovementSetting> settingSupplier) {
		SETTING_SUPPLIERS.register(block, settingSupplier);
	}

	public static void register(Block block, Supplier<ContraptionMovementSetting> settingSupplier) {
		SETTING_SUPPLIERS.register(block, settingSupplier);
	}

	@Nullable
	public static ContraptionMovementSetting get(Block block) {
		if (block instanceof IMovementSettingProvider provider)
			return provider.getContraptionMovementSetting();
		Supplier<ContraptionMovementSetting> supplier = SETTING_SUPPLIERS.get(block);
		if (supplier == null)
			return null;
		return supplier.get();
	}

	public static boolean allAre(Collection<StructureTemplate.StructureBlockInfo> blocks, ContraptionMovementSetting are) {
		return blocks.stream().anyMatch(b -> get(b.state.getBlock()) == are);
	}

	public static boolean isNoPickup(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return allAre(blocks, ContraptionMovementSetting.NO_PICKUP);
	}

	public static void registerDefaults() {
		register(Blocks.SPAWNER, () -> AllConfigs.server().kinetics.spawnerMovement.get());
		register(Blocks.BUDDING_AMETHYST, () -> AllConfigs.server().kinetics.amethystMovement.get());
		register(Blocks.OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		register(Blocks.CRYING_OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		register(Blocks.RESPAWN_ANCHOR, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		register(Blocks.REINFORCED_DEEPSLATE, () -> AllConfigs.server().kinetics.reinforcedDeepslateMovement.get());
	}

	public interface IMovementSettingProvider extends IForgeBlock {
		ContraptionMovementSetting getContraptionMovementSetting();
	}
}
