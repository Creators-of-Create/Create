package com.simibubi.create.modules.kinetics.base;

import com.simibubi.create.modules.kinetics.RotationPropagator;

import net.minecraft.block.Block;
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

public abstract class KineticBlock extends Block implements IRotate {

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

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//		RotationPropagator.handleAdded(worldIn, pos);
	}

	@Override
	public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
		RotationPropagator.handleAdded(worldIn.getWorld(), pos, (KineticTileEntity) worldIn.getTileEntity(pos));
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
