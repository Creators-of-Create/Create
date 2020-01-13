package com.simibubi.create.foundation.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OxidizingBlock extends Block {

	public static final BooleanProperty OXIDIZED = BooleanProperty.create("oxidized");
	private float chance;

	public OxidizingBlock(Properties properties, float chance) {
		super(properties);
		this.chance = chance;
		setDefaultState(getDefaultState().with(OXIDIZED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(OXIDIZED));
	}

	@Override
	public boolean ticksRandomly(BlockState state) {
		return super.ticksRandomly(state) || !state.get(OXIDIZED);
	}
	
	@Override
	public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (worldIn.getRandom().nextFloat() <= chance)
			for (Direction facing : Direction.values()) {
				BlockPos neighbourPos = pos.offset(facing);
				if (!worldIn.isBlockPresent(neighbourPos))
					continue;
				if (!Block.hasSolidSide(worldIn.getBlockState(neighbourPos), worldIn, neighbourPos,
						facing.getOpposite()))
					continue;
				worldIn.setBlockState(pos, state.with(OXIDIZED, true));
				break;
			}
	}

}
