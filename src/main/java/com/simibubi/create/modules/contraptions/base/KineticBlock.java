package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.modules.contraptions.RotationPropagator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class KineticBlock extends Block implements IRotate {

	protected static final Palette color = Palette.Red;

	public KineticBlock(Properties properties) {
		super(properties);
	}

	// IRotate

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face) {
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
		if (worldIn.isRemote())
			return;
		RotationPropagator.handleAdded(worldIn.getWorld(), pos, tileEntity);
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return hasStaticPart() && layer == getRenderLayer();
	}

	protected abstract boolean hasStaticPart();

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity == null || !(tileEntity instanceof KineticTileEntity))
			return;
		if (worldIn.isRemote)
			return;

		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.queueRotationIndicators();
	}

	public float getParticleTargetRadius() {
		return .65f;
	}

	public float getParticleInitialRadius() {
		return .75f;
	}

}
