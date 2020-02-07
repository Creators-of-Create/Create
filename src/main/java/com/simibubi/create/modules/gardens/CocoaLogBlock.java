package com.simibubi.create.modules.gardens;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.simibubi.create.config.AllConfigs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CocoaLogBlock extends RotatedPillarBlock implements IGrowable {

	public static IntegerProperty AGE = BlockStateProperties.AGE_0_2;

	public CocoaLogBlock() {
		super(Properties.from(Blocks.JUNGLE_LOG).tickRandomly());
	}

	@Override
	public boolean canGrow(IBlockReader arg0, BlockPos arg1, BlockState state, boolean arg3) {
		return true;
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (!worldIn.isAreaLoaded(pos, 1))
			return; // Forge: prevent loading unloaded chunks when checking neighbor's light
		grow(worldIn, random, pos, state);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AGE);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean canUseBonemeal(World arg0, Random arg1, BlockPos arg2, BlockState arg3) {
		return true;
	}

	@Override
	public void grow(World world, Random random, BlockPos pos, BlockState state) {
		if (random.nextDouble() > AllConfigs.SERVER.curiosities.cocoaLogGrowthSpeed.get() / 100D)
			return;

		int age = state.get(AGE);

		if (age < 2) {
			world.setBlockState(pos, state.with(AGE, age + 1));
			return;
		}

		List<Direction> shuffledDirections = Arrays.asList(Direction.values());
		Collections.shuffle(shuffledDirections);

		for (Direction facing : shuffledDirections) {
			if (facing.getAxis().isVertical())
				continue;
			if (facing.getAxis() == state.get(AXIS))
				continue;
			if (!world.getBlockState(pos.offset(facing)).getMaterial().isReplaceable())
				continue;

			world.setBlockState(pos.offset(facing),
					Blocks.COCOA.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, facing.getOpposite())
							.with(BlockStateProperties.AGE_0_2, 0));
			break;
		}

	}

}
