package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CrushingWheelBlock extends RotatedPillarKineticBlock {

	public CrushingWheelBlock() {
		super(Properties.from(Blocks.DIORITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CrushingWheelTileEntity();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}
	
	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}
	
	@Override
	protected boolean hasStaticPart() {
		return false;
	}

}
