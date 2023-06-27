package com.simibubi.create.foundation.placement;

import java.util.function.Function;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;

public class PlacementOffset {

	private final boolean success;
	private Vec3i pos;
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

	public static PlacementOffset success(Vec3i pos) {
		return success().at(pos);
	}

	public static PlacementOffset success(Vec3i pos, Function<BlockState, BlockState> transform) {
		return success().at(pos).withTransform(transform);
	}

	public PlacementOffset at(Vec3i pos) {
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

	public Vec3i getPos() {
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

	public boolean isReplaceable(Level world) {
		if (!success)
			return false;

		return world.getBlockState(new BlockPos(pos)).canBeReplaced();
	}

	public InteractionResult placeInWorld(Level world, BlockItem blockItem, Player player, InteractionHand hand, BlockHitResult ray) {

		if (!isReplaceable(world))
			return InteractionResult.PASS;

		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		UseOnContext context = new UseOnContext(player, hand, ray);
		BlockPos newPos = new BlockPos(pos);
		ItemStack stackBefore = player.getItemInHand(hand)
			.copy();

		if (!world.mayInteract(player, newPos))
			return InteractionResult.PASS;

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
			return InteractionResult.FAIL;
		}

		BlockState newState = world.getBlockState(newPos);
		SoundType soundtype = newState.getSoundType(world, newPos, player);
		world.playSound(null, newPos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

		player.awardStat(Stats.ITEM_USED.get(blockItem));
		newState.getBlock()
			.setPlacedBy(world, newPos, newState, player, stackBefore);

		if (player instanceof ServerPlayer)
			CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, newPos, context.getItemInHand());

		if (!player.isCreative())
			context.getItemInHand().shrink(1);

		return InteractionResult.SUCCESS;
	}
}
