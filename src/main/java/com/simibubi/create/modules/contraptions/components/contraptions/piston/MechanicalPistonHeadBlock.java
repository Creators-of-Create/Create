package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalPistonHeadBlock extends ProperDirectionalBlock {

	public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;

	public MechanicalPistonHeadBlock() {
		super(Properties.from(Blocks.PISTON_HEAD));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(TYPE);
		super.fillStateContainer(builder);
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.NORMAL;
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

		for (int offset = 1; offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset++) {
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
		return AllShapes.MECHANICAL_PISTON_HEAD.get(state.get(FACING));
	}
}
