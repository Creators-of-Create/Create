package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.components.contraptions.IPortableBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class HarvesterBlock extends HorizontalBlock implements IPortableBlock {

	public static MovementBehaviour MOVEMENT = new HarvesterMovementBehaviour();

	public HarvesterBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new HarvesterTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
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

//	@Override // TOOD 1.15 register layer
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.CUTOUT;
//	}

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
			if (AllBlocks.HARVESTER.typeOf(blockState))
				facing = blockState.get(HORIZONTAL_FACING);
			else
				facing = context.getFace();
		}
		return getDefaultState().with(HORIZONTAL_FACING, facing);
	}

	@Override
	public MovementBehaviour getMovementBehaviour() {
		return MOVEMENT;
	}

}
