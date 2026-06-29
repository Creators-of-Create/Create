package com.simibubi.create.api.contraption;

import java.util.Collection;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.neoforged.neoforge.common.extensions.IBlockExtension;

/**
 * Defines whether a block is movable by contraptions.
 * This is used as a fallback check for {@link BlockMovementChecks#isMovementAllowed(BlockState, Level, BlockPos)}.
 * The registry uses suppliers, so the setting of a block can change. This is useful for config options.
 */
public enum ContraptionMovementSetting {
	/**
	 * Block is fully movable with no restrictions.
	 */
	MOVABLE,
	/**
	 * Block can be mounted and moved, but if it's on a minecart contraption, the contraption cannot be picked up.
	 */
	NO_PICKUP,
	/**
	 * Block cannot ever be moved by a contraption.
	 */
	UNMOVABLE;

	public static final SimpleRegistry<Block, Supplier<ContraptionMovementSetting>> REGISTRY = SimpleRegistry.create();

	/**
	 * Shortcut that gets the block of the given state.
	 */
	@Nullable
	public static ContraptionMovementSetting get(BlockState state) {
		return get(state.getBlock());
	}

	/**
	 * Get the current movement setting of the given block.
	 */
	@Nullable
	public static ContraptionMovementSetting get(Block block) {
		if (block instanceof MovementSettingProvider provider)
			return provider.getContraptionMovementSetting();
		Supplier<ContraptionMovementSetting> supplier = REGISTRY.get(block);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Check if any of the blocks in the collection match the given setting.
	 */
	public static boolean anyAre(Collection<StructureTemplate.StructureBlockInfo> blocks, ContraptionMovementSetting setting) {
		return blocks.stream().anyMatch(b -> get(b.state().getBlock()) == setting);
	}

	/**
	 * Check if any of the blocks in the collection forbid pickup.
	 */
	public static boolean isNoPickup(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return anyAre(blocks, ContraptionMovementSetting.NO_PICKUP);
	}

	/**
	 * Interface that may optionally be implemented on a Block implementation which will be queried instead of the registry.
	 */
	public interface MovementSettingProvider extends IBlockExtension {
		ContraptionMovementSetting getContraptionMovementSetting();
	}
}
