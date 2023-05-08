package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SailBlock extends WrenchableDirectionalBlock {

	public static SailBlock frame(Properties properties) {
		return new SailBlock(properties, true, null);
	}

	public static SailBlock withCanvas(Properties properties, DyeColor color) {
		return new SailBlock(properties, false, color);
	}

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	protected final boolean frame;
	protected final DyeColor color;

	protected SailBlock(Properties properties, boolean frame, DyeColor color) {
		super(properties);
		this.frame = frame;
		this.color = color;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
		if (frame || color == DyeColor.WHITE) {
			super.fillItemCategory(tab, items);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state.setValue(FACING, state.getValue(FACING)
			.getOpposite());
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (!player.isShiftKeyDown() && player.mayBuild()) {
			if (placementHelper.matchesItem(heldItem)) {
				placementHelper.getOffset(player, world, state, pos, ray)
					.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
				return InteractionResult.SUCCESS;
			}
		}

		if (heldItem.getItem() instanceof ShearsItem) {
			if (!world.isClientSide)
				world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
				applyDye(state, world, pos, ray.getLocation(), null);
			return InteractionResult.SUCCESS;
		}

		if (frame)
			return InteractionResult.PASS;

		DyeColor color = DyeColor.getColor(heldItem);
		if (color != null) {
			if (!world.isClientSide)
				world.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.1f - world.random.nextFloat() * .2f);
				applyDye(state, world, pos, ray.getLocation(), color);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	public void applyDye(BlockState state, Level world, BlockPos pos, Vec3 hit, @Nullable DyeColor color) {
		BlockState newState =
			(color == null ? AllBlocks.SAIL_FRAME : AllBlocks.DYED_SAILS.get(color)).getDefaultState();
		newState = BlockHelper.copyProperties(state, newState);

		// Dye the block itself
		if (state != newState) {
			world.setBlockAndUpdate(pos, newState);
			return;
		}

		// Dye all adjacent
		List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, hit, state.getValue(FACING)
			.getAxis());
		for (Direction d : directions) {
			BlockPos offset = pos.relative(d);
			BlockState adjacentState = world.getBlockState(offset);
			Block block = adjacentState.getBlock();
			if (!(block instanceof SailBlock) || ((SailBlock) block).frame)
				continue;
			if (state.getValue(FACING) != adjacentState.getValue(FACING))
				continue;
			if (state == adjacentState)
				continue;
			world.setBlockAndUpdate(offset, newState);
			return;
		}

		// Dye all the things
		List<BlockPos> frontier = new ArrayList<>();
		frontier.add(pos);
		Set<BlockPos> visited = new HashSet<>();
		int timeout = 100;
		while (!frontier.isEmpty()) {
			if (timeout-- < 0)
				break;

			BlockPos currentPos = frontier.remove(0);
			visited.add(currentPos);

			for (Direction d : Iterate.directions) {
				if (d.getAxis() == state.getValue(FACING)
					.getAxis())
					continue;
				BlockPos offset = currentPos.relative(d);
				if (visited.contains(offset))
					continue;
				BlockState adjacentState = world.getBlockState(offset);
				Block block = adjacentState.getBlock();
				if (!(block instanceof SailBlock) || ((SailBlock) block).frame && color != null)
					continue;
				if (adjacentState.getValue(FACING) != state.getValue(FACING))
					continue;
				if (state != adjacentState)
					world.setBlockAndUpdate(offset, newState);
				frontier.add(offset);
				visited.add(offset);
			}
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return (frame ? AllShapes.SAIL_FRAME : AllShapes.SAIL).get(state.getValue(FACING));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter p_220071_2_, BlockPos p_220071_3_,
		CollisionContext p_220071_4_) {
		if (frame)
			return AllShapes.SAIL_FRAME_COLLISION.get(state.getValue(FACING));
		return getShape(state, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos,
		Player player) {
		ItemStack pickBlock = super.getCloneItemStack(state, target, world, pos, player);
		if (pickBlock.isEmpty())
			return AllBlocks.SAIL.get()
				.getCloneItemStack(state, target, world, pos, player);
		return pickBlock;
	}

	@Override
	public void fallOn(Level p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, float p_152430_) {
		if (frame)
			super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_);
		super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, 0);
	}

	public void updateEntityAfterFallOn(BlockGetter p_176216_1_, Entity p_176216_2_) {
		if (frame || p_176216_2_.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(p_176216_1_, p_176216_2_);
		} else {
			this.bounce(p_176216_2_);
		}
	}

	private void bounce(Entity p_226860_1_) {
		Vec3 Vector3d = p_226860_1_.getDeltaMovement();
		if (Vector3d.y < 0.0D) {
			double d0 = p_226860_1_ instanceof LivingEntity ? 1.0D : 0.8D;
			p_226860_1_.setDeltaMovement(Vector3d.x, -Vector3d.y * (double) 0.26F * d0, Vector3d.z);
		}

	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	public boolean isFrame() {
		return frame;
	}

	public DyeColor getColor() {
		return color;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> AllBlocks.SAIL.isIn(i) || AllBlocks.SAIL_FRAME.isIn(i);
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return s -> s.getBlock() instanceof SailBlock;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
				state.getValue(SailBlock.FACING)
					.getAxis(),
				dir -> world.getBlockState(pos.relative(dir))
					.getMaterial()
					.isReplaceable());

			if (directions.isEmpty())
				return PlacementOffset.fail();
			else {
				return PlacementOffset.success(pos.relative(directions.get(0)),
					s -> s.setValue(FACING, state.getValue(FACING)));
			}
		}
	}
}
