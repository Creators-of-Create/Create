package com.simibubi.create.foundation.utility.placement;

import java.util.function.Function;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

public class PlacementOffset {

	private final boolean success;
	private Vector3i pos;
	private Function<BlockState, BlockState> stateTransform;
	private BlockState ghostState;

	private PlacementOffset(boolean success) {
		this.success = success;
		this.pos = BlockPos.ZERO;
		this.stateTransform = Function.identity();
		this.ghostState = null;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false);
	}

	public static PlacementOffset success() {
		return new PlacementOffset(true);
	}

	public static PlacementOffset success(Vector3i pos) {
		return success().at(pos);
	}

	public static PlacementOffset success(Vector3i pos, Function<BlockState, BlockState> transform) {
		return success().at(pos).withTransform(transform);
	}

	public PlacementOffset at(Vector3i pos) {
		this.pos = pos;
		return this;
	}

	public PlacementOffset withTransform(Function<BlockState, BlockState> stateTransform) {
		this.stateTransform = stateTransform;
		return this;
	}

	public PlacementOffset withGhostState(BlockState ghostState) {
		this.ghostState = ghostState;
		return this;
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vector3i getPos() {
		return pos;
	}

	public BlockPos getBlockPos() {
		if (pos instanceof BlockPos)
			return (BlockPos) pos;

		return new BlockPos(pos);
	}

	public Function<BlockState, BlockState> getTransform() {
		return stateTransform;
	}

	public boolean hasGhostState() {
		return ghostState != null;
	}

	public BlockState getGhostState() {
		return ghostState;
	}

	public boolean isReplaceable(World world) {
		if (!success)
			return false;

		return world.getBlockState(new BlockPos(pos)).getMaterial().isReplaceable();
	}

	public ActionResultType placeInWorld(World world, BlockItem blockItem, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {

		if (!isReplaceable(world))
			return ActionResultType.PASS;

		if (world.isClientSide)
			return ActionResultType.SUCCESS;

		ItemUseContext context = new ItemUseContext(player, hand, ray);
		BlockPos newPos = new BlockPos(pos);

		if (!world.mayInteract(player, newPos))
			return ActionResultType.PASS;

		BlockState state = stateTransform.apply(blockItem.getBlock().defaultBlockState());
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			FluidState fluidState = world.getFluidState(newPos);
			state = state.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
		}

		BlockSnapshot snapshot = BlockSnapshot.create(world.dimension(), world, newPos);
		world.setBlockAndUpdate(newPos, state);

		BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snapshot, IPlacementHelper.ID, player);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			snapshot.restore(true, false);
			return ActionResultType.FAIL;
		}

		BlockState newState = world.getBlockState(newPos);
		SoundType soundtype = newState.getSoundType(world, newPos, player);
		world.playSound(null, newPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

		player.awardStat(Stats.ITEM_USED.get(blockItem));

		if (player instanceof ServerPlayerEntity)
			CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, newPos, context.getItemInHand());

		if (!player.isCreative())
			context.getItemInHand().shrink(1);

		return ActionResultType.SUCCESS;
	}
}
