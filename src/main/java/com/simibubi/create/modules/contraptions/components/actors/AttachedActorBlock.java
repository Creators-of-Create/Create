package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.IWrenchable;
import com.simibubi.create.modules.contraptions.components.contraptions.IPortableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public abstract class AttachedActorBlock extends HorizontalBlock implements IPortableBlock, IWrenchable {

	public AttachedActorBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}
	
	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.FAIL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction direction = state.get(HORIZONTAL_FACING);
		return AllShapes.HARVESTER_BASE.get(direction);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos offset = pos.offset(direction.getOpposite());
		return Block.hasSolidSide(worldIn.getBlockState(offset), worldIn, offset, direction);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing;
		if (context.getFace().getAxis().isVertical())
			facing = context.getPlacementHorizontalFacing().getOpposite();
		else {
			BlockState blockState =
				context.getWorld().getBlockState(context.getPos().offset(context.getFace().getOpposite()));
			if (blockState.getBlock() instanceof AttachedActorBlock)
				facing = blockState.get(HORIZONTAL_FACING);
			else
				facing = context.getFace();
		}
		return getDefaultState().with(HORIZONTAL_FACING, facing);
	}

}
