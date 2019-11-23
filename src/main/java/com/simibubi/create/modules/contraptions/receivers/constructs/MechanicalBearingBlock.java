package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalBearingBlock extends DirectionalKineticBlock
		implements IWithTileEntity<MechanicalBearingTileEntity> {

	public MechanicalBearingBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalBearingTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		withTileEntityDo(worldIn, pos, MechanicalBearingTileEntity::neighbourChanged);
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

}
