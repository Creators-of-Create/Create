package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import static com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.isPistonHead;

import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.IWrenchable;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
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

public class PistonExtensionPoleBlock extends ProperDirectionalBlock implements IWrenchable {

	public PistonExtensionPoleBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Axis axis = state.get(FACING)
			.getAxis();
		Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset +=
				modifier) {
				BlockPos currentPos = pos.offset(direction, offset);
				BlockState block = worldIn.getBlockState(currentPos);

				if (isExtensionPole(block) && axis == block.get(FACING)
					.getAxis())
					continue;

				if (isPiston(block) && block.get(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonBase = currentPos;

				if (isPistonHead(block) && block.get(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonHead = currentPos;

				break;
			}
		}

		if (pistonHead != null && pistonBase != null && worldIn.getBlockState(pistonHead)
			.get(BlockStateProperties.FACING) == worldIn.getBlockState(pistonBase)
				.get(BlockStateProperties.FACING)) {

			final BlockPos basePos = pistonBase;
			BlockPos.getAllInBox(pistonBase, pistonHead)
				.filter(p -> !p.equals(pos) && !p.equals(basePos))
				.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
			worldIn.setBlockState(basePos, worldIn.getBlockState(basePos)
				.with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.get(FACING)
			.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace()
			.getOpposite());
	}

}
