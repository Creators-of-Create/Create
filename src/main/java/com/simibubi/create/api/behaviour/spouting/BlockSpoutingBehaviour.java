package com.simibubi.create.api.behaviour.spouting;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;


/**
 * Interface for custom block-filling behavior for spouts.
 * <p>
 * Behaviors are queried by block first, through {@link #BY_BLOCK}. If no behavior was provided,
 * they are then queried by block entity type, through {@link #BY_BLOCK_ENTITY}.
 * @see StateChangingBehavior
 * @see CauldronSpoutingBehavior
 */
@FunctionalInterface
public interface BlockSpoutingBehaviour {
	SimpleRegistry<Block, BlockSpoutingBehaviour> BY_BLOCK = SimpleRegistry.create();
	SimpleRegistry<BlockEntityType<?>, BlockSpoutingBehaviour> BY_BLOCK_ENTITY = SimpleRegistry.create();

	/**
	 * Get the behavior that should be used for the block at the given location.
	 * Queries both the block and the block entity if needed.
	 */
	@Nullable
	static BlockSpoutingBehaviour get(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		BlockSpoutingBehaviour byBlock = BY_BLOCK.get(state.getBlock());
		if (byBlock != null)
			return byBlock;

		BlockEntity be = level.getBlockEntity(pos);
		if (be == null)
			return null;

		return BY_BLOCK_ENTITY.get(be.getType());
	}

	/**
	 * While idle, spouts will query the behavior provided by the block below it.
	 * If one is present, this method will be called every tick with simulate == true.
	 * <p>
	 * When a value greater than 0 is returned, the spout will begin processing. It will call this method again
	 * with simulate == false, which is when any filling behavior should actually occur.
	 * <p>
	 * This method is only called on the server side, except for in Ponder.
	 * @param level          The current level
	 * @param pos            The position of the affected block
	 * @param spout          The spout block entity that is calling this
	 * @param availableFluid A copy of the fluidStack that is available, modifying this will do nothing, return the amount to be subtracted instead
	 * @param simulate       Whether the spout is testing or actually performing this behaviour
	 * @return The amount filled into the block, 0 to idle/cancel
	 */
	int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate);
}
