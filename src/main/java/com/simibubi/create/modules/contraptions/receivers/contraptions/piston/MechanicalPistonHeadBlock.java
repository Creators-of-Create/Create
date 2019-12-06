package com.simibubi.create.modules.contraptions.receivers.contraptions.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.modules.contraptions.receivers.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalPistonHeadBlock extends ProperDirectionalBlock implements IWithoutBlockItem {

	public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;

	public static final VoxelShape AXIS_SHAPE_X = makeCuboidShape(0, 6, 6, 16, 10, 10),
			AXIS_SHAPE_Y = makeCuboidShape(6, 0, 6, 10, 16, 10), AXIS_SHAPE_Z = makeCuboidShape(6, 6, 0, 10, 10, 16),

			TOP_SHAPE_UP = makeCuboidShape(0, 12, 0, 16, 16, 16), TOP_SHAPE_DOWN = makeCuboidShape(0, 0, 0, 16, 4, 16),
			TOP_SHAPE_EAST = makeCuboidShape(12, 0, 0, 16, 16, 16),
			TOP_SHAPE_WEST = makeCuboidShape(0, 0, 0, 4, 16, 16),
			TOP_SHAPE_SOUTH = makeCuboidShape(0, 0, 12, 16, 16, 16),
			TOP_SHAPE_NORTH = makeCuboidShape(0, 0, 0, 16, 16, 4),

			EXTENSION_SHAPE_UP = VoxelShapes.or(AXIS_SHAPE_Y, TOP_SHAPE_UP),
			EXTENSION_SHAPE_DOWN = VoxelShapes.or(AXIS_SHAPE_Y, TOP_SHAPE_DOWN),
			EXTENSION_SHAPE_EAST = VoxelShapes.or(AXIS_SHAPE_X, TOP_SHAPE_EAST),
			EXTENSION_SHAPE_WEST = VoxelShapes.or(AXIS_SHAPE_X, TOP_SHAPE_WEST),
			EXTENSION_SHAPE_SOUTH = VoxelShapes.or(AXIS_SHAPE_Z, TOP_SHAPE_SOUTH),
			EXTENSION_SHAPE_NORTH = VoxelShapes.or(AXIS_SHAPE_Z, TOP_SHAPE_NORTH);

	public MechanicalPistonHeadBlock() {
		super(Properties.from(Blocks.PISTON_HEAD));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(TYPE);
		super.fillStateContainer(builder);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		return new ItemStack(AllBlocks.PISTON_POLE.get());
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Direction direction = state.get(FACING);
		BlockPos pistonHead = pos;
		BlockPos pistonBase = null;

		for (int offset = 1; offset < CreateConfig.parameters.maxPistonPoles.get(); offset++) {
			BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis())
				continue;

			if ((AllBlocks.MECHANICAL_PISTON.typeOf(block) || AllBlocks.STICKY_MECHANICAL_PISTON.typeOf(block))
					&& block.get(BlockStateProperties.FACING) == direction) {
				pistonBase = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
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

		switch (state.get(FACING)) {
		case DOWN:
			return EXTENSION_SHAPE_DOWN;
		case EAST:
			return EXTENSION_SHAPE_EAST;
		case NORTH:
			return EXTENSION_SHAPE_NORTH;
		case SOUTH:
			return EXTENSION_SHAPE_SOUTH;
		case UP:
			return EXTENSION_SHAPE_UP;
		case WEST:
			return EXTENSION_SHAPE_WEST;
		}

		return VoxelShapes.empty();
	}
}
