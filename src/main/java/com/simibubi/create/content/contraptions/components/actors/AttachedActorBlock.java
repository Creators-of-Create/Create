package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.BlockHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AttachedActorBlock extends HorizontalBlock implements IWrenchable {

	protected AttachedActorBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.FAIL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction direction = state.getValue(FACING);
		return AllShapes.HARVESTER_BASE.get(direction);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction direction = state.getValue(FACING);
		BlockPos offset = pos.relative(direction.getOpposite());
		return BlockHelper.hasBlockSolidSide(worldIn.getBlockState(offset), worldIn, offset, direction);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing;
		if (context.getClickedFace().getAxis().isVertical())
			facing = context.getHorizontalDirection().getOpposite();
		else {
			BlockState blockState =
				context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
			if (blockState.getBlock() instanceof AttachedActorBlock)
				facing = blockState.getValue(FACING);
			else
				facing = context.getClickedFace();
		}
		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}
	
}
