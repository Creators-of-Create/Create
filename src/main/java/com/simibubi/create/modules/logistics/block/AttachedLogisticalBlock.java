package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.logistics.block.transposer.TransposerBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class AttachedLogisticalBlock extends HorizontalBlock {

	public static final BooleanProperty UPWARD = BooleanProperty.create("upward");

	public AttachedLogisticalBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	protected abstract boolean isVertical();

	protected abstract BlockState getVerticalDefaultState();
	
	protected abstract BlockState getHorizontalDefaultState();

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = getVerticalDefaultState();
			state = state.with(UPWARD, context.getFace() != Direction.UP);
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state;
	}
	
	@Override
	public ResourceLocation getLootTable() {
		if (isVertical())
			return getHorizontalDefaultState().getBlock().getLootTable();
		return super.getLootTable();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		if (isVertical())
			return getHorizontalDefaultState().getBlock().getPickBlock(state, target, world, pos, player);
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = getBlockFacing(state);
		return canAttachToSide(worldIn, pos, facing);
	}

	protected boolean canAttachToSide(IWorldReader worldIn, BlockPos pos, Direction facing) {
		BlockPos neighbourPos = pos.offset(facing);
		BlockState neighbour = worldIn.getBlockState(neighbourPos);

		if (neighbour.getBlock() instanceof TransposerBlock)
			return false;
		if (AllBlocks.BELT.typeOf(neighbour))
			return BeltBlock.canAccessFromSide(facing, neighbour);
		return !neighbour.getShape(worldIn, pos).isEmpty();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = getBlockFacing(state);
		if (fromPos.equals(pos.offset(blockFacing))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	public static Direction getBlockFacing(BlockState state) {
		if (isVertical(state))
			return state.get(UPWARD) ? Direction.UP : Direction.DOWN;
		return state.get(HORIZONTAL_FACING);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		if (isVertical())
			builder.add(UPWARD);
		super.fillStateContainer(builder.add(HORIZONTAL_FACING));
	}

	public static boolean isVertical(BlockState state) {
		Block block = state.getBlock();
		return ((block instanceof AttachedLogisticalBlock)
				&& (((AttachedLogisticalBlock) state.getBlock())).isVertical());
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

}
