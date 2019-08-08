package com.simibubi.create.modules.kinetics.relays;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BeltPulleyBlock extends AxisBlock {

	public BeltPulleyBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return makeCuboidShape(-16, -16, -16, 32, 32, 32);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltPulleyTileEntity();
	}
	
	@Override
	protected boolean hasStaticPart() {
		return false; // static addons like chutes
	}

}
