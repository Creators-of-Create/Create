package com.simibubi.create.modules.contraptions.components.constructs.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.components.constructs.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PistonPoleBlock extends ProperDirectionalBlock {

	public PistonPoleBlock() {
		super(Properties.from(Blocks.PISTON_HEAD));
		setDefaultState(getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Axis axis = state.get(FACING).getAxis();
		Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < CreateConfig.parameters.maxPistonPoles
					.get(); offset += modifier) {
				BlockPos currentPos = pos.offset(direction, offset);
				BlockState block = worldIn.getBlockState(currentPos);

				if (AllBlocks.PISTON_POLE.typeOf(block) && axis == block.get(FACING).getAxis())
					continue;

				if ((AllBlocks.MECHANICAL_PISTON.typeOf(block) || AllBlocks.STICKY_MECHANICAL_PISTON.typeOf(block))
						&& block.get(BlockStateProperties.FACING).getAxis() == axis) {
					pistonBase = currentPos;
				}

				if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(block)
						&& block.get(BlockStateProperties.FACING).getAxis() == axis) {
					pistonHead = currentPos;
				}

				break;
			}
		}

		if (pistonHead != null && pistonBase != null
				&& worldIn.getBlockState(pistonHead).get(BlockStateProperties.FACING) == worldIn
						.getBlockState(pistonBase).get(BlockStateProperties.FACING)) {

			final BlockPos basePos = pistonBase;
			BlockPos.getAllInBox(pistonBase, pistonHead).filter(p -> !p.equals(pos) && !p.equals(basePos))
					.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
			worldIn.setBlockState(basePos,
					worldIn.getBlockState(basePos).with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.get(FACING).getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace().getOpposite());
	}

}
