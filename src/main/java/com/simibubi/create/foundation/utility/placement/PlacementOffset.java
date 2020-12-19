package com.simibubi.create.foundation.utility.placement;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.function.Function;

public class PlacementOffset {

	private final boolean success;
	private final Vec3i pos;
	private final Function<BlockState, BlockState> stateTransform;

	private PlacementOffset(boolean success, Vec3i pos, Function<BlockState, BlockState> transform) {
		this.success = success;
		this.pos = pos;
		this.stateTransform = transform == null ? Function.identity() : transform;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false, Vec3i.NULL_VECTOR, null);
	}

	public static PlacementOffset success(Vec3i pos) {
		return new PlacementOffset(true, pos, null);
	}

	public static PlacementOffset success(Vec3i pos, Function<BlockState, BlockState> transform) {
		return new PlacementOffset(true, pos, transform);
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vec3i getPos() {
		return pos;
	}

	public Function<BlockState, BlockState> getTransform() {
		return stateTransform;
	}

	public boolean isReplaceable(World world) {
		if (!success)
			return false;

		return world.getBlockState(new BlockPos(pos)).getMaterial().isReplaceable();
	}

	public void placeInWorld(World world, BlockItem blockItem, PlayerEntity player, ItemStack item) {
		placeInWorld(world, blockItem.getBlock().getDefaultState(), player, item);
	}

	public void placeInWorld(World world, BlockState defaultState, PlayerEntity player, ItemStack item) {
		if (world.isRemote)
			return;

		BlockPos newPos = new BlockPos(pos);
		BlockState state = stateTransform.apply(defaultState);
		if (state.has(BlockStateProperties.WATERLOGGED)) {
			IFluidState fluidState = world.getFluidState(newPos);
			state = state.with(BlockStateProperties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		}

		world.setBlockState(newPos, state);

		if (!player.isCreative())
			item.shrink(1);
	}
}
