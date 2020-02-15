package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public abstract class BearingBlock extends DirectionalKineticBlock {

	public BearingBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	protected boolean turnBackOnWrenched() {
		return true;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

}
