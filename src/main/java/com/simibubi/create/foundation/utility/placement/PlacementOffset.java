package com.simibubi.create.foundation.utility.placement;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

import java.util.function.Function;

public class PlacementOffset {

	private final boolean success;
	private final Vector3i pos;
	private final Function<BlockState, BlockState> stateTransform;

	private PlacementOffset(boolean success, Vector3i pos, Function<BlockState, BlockState> transform) {
		this.success = success;
		this.pos = pos;
		this.stateTransform = transform == null ? Function.identity() : transform;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false, Vector3i.NULL_VECTOR, null);
	}

	public static PlacementOffset success(Vector3i pos) {
		return new PlacementOffset(true, pos, null);
	}

	public static PlacementOffset success(Vector3i pos, Function<BlockState, BlockState> transform) {
		return new PlacementOffset(true, pos, transform);
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vector3i getPos() {
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
	
	public ActionResultType placeInWorld(World world, BlockItem blockItem, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {

		ItemUseContext context = new ItemUseContext(player, hand, ray);

		BlockPos newPos = new BlockPos(pos);

		if (!world.isBlockModifiable(player, newPos))
			return ActionResultType.PASS;

		if (!isReplaceable(world))
			return ActionResultType.PASS;

		BlockState state = stateTransform.apply(blockItem.getBlock().getDefaultState());
		if (state.contains(BlockStateProperties.WATERLOGGED)) {
			FluidState fluidState = world.getFluidState(newPos);
			state = state.with(BlockStateProperties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		}

		BlockSnapshot snapshot = BlockSnapshot.create(world.getRegistryKey(), world, newPos);
		world.setBlockState(newPos, state);

		BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snapshot, IPlacementHelper.ID, player);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			snapshot.restore(true, false);
			return ActionResultType.FAIL;
		}

		BlockState newState = world.getBlockState(newPos);
		SoundType soundtype = newState.getSoundType(world, newPos, player);
		world.playSound(player, newPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

		player.addStat(Stats.ITEM_USED.get(blockItem));

		if (world.isRemote)
			return ActionResultType.SUCCESS;

		if (player instanceof ServerPlayerEntity)
			CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, newPos, context.getItem());

		if (!player.isCreative())
			context.getItem().shrink(1);

		return ActionResultType.SUCCESS;
	}
}
