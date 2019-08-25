package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.foundation.block.InfoBlock;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.modules.contraptions.RotationPropagator;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class KineticBlock extends InfoBlock implements IRotate {

	protected static final Palette color = Palette.Red;
	
	public KineticBlock(Properties properties) {
		super(properties);
	}

	// IRotate

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public boolean isGearTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return null;
	}

	// Block

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

	@SuppressWarnings("deprecation")
	@Override
	public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
		super.updateNeighbors(stateIn, worldIn, pos, flags);
		KineticTileEntity tileEntity = (KineticTileEntity) worldIn.getTileEntity(pos);
		if (tileEntity == null)
			return;
		RotationPropagator.handleAdded(worldIn.getWorld(), pos, tileEntity);
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return hasStaticPart() && layer == BlockRenderLayer.SOLID;
	}

	protected abstract boolean hasStaticPart();

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

}
