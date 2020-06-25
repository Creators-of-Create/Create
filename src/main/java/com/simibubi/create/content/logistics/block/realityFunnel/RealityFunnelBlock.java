package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RealityFunnelBlock extends ProperDirectionalBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public RealityFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getFace();
		if (facing.getAxis()
			.isVertical()
			&& context.getWorld()
				.getBlockState(context.getPos()
					.offset(facing.getOpposite()))
				.getBlock() instanceof ChuteBlock)
			facing = facing.getOpposite();
		return getDefaultState().with(FACING, facing)
			.with(POWERED, context.getWorld()
				.isBlockPowered(context.getPos()));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.REALITY_FUNNEL.get(state.get(FACING));
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState p_196271_3_, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		Direction facing = state.get(FACING);
		if (facing.getAxis()
			.isHorizontal()) {
			if (direction == Direction.DOWN) {
				BlockState equivalentFunnel = AllBlocks.BELT_FUNNEL.getDefaultState()
					.with(BeltFunnelBlock.HORIZONTAL_FACING, facing)
					.with(BeltFunnelBlock.POWERED, state.get(POWERED));
				if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
			if (direction == facing) {
				BlockState equivalentFunnel = AllBlocks.CHUTE_FUNNEL.getDefaultState()
					.with(ChuteFunnelBlock.HORIZONTAL_FACING, facing)
					.with(ChuteFunnelBlock.POWERED, state.get(POWERED));
				if (ChuteFunnelBlock.isOnValidChute(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
			if (direction == facing.getOpposite()) {
				BlockState equivalentFunnel = AllBlocks.CHUTE_FUNNEL.getDefaultState()
					.with(ChuteFunnelBlock.HORIZONTAL_FACING, facing.getOpposite())
					.cycle(ChuteFunnelBlock.PUSHING)
					.with(ChuteFunnelBlock.POWERED, state.get(POWERED));
				if (ChuteFunnelBlock.isOnValidChute(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
		}
		return state;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.offset(state.get(FACING)
			.getOpposite()))
			.getBlock();
		return !(block instanceof RealityFunnelBlock) && !(block instanceof HorizontalInteractionFunnelBlock);
	}

}
