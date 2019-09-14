package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GearboxBlock extends RotatedPillarKineticBlock {

	public GearboxBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new GearboxTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() != state.get(AXIS);
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

}
