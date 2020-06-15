package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.item.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public abstract class KineticBlock extends Block implements IRotate {

	protected static final Palette color = Palette.Red;

	public KineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
		for (ToolType toolType : player.getHeldItemMainhand().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}
		return super.canHarvestBlock(state, world, pos, player);
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		// onBlockAdded is useless for init, as sometimes the TE gets re-instantiated
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

	@Override
	@SuppressWarnings("deprecation")
	public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
		super.updateNeighbors(stateIn, worldIn, pos, flags);
		if (worldIn.isRemote())
			return;

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		// Remove previous information when block is added
		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.warnOfMovement();
		kte.clearKineticInformation();
		kte.updateSpeed = true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (worldIn.isRemote)
			return;

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.effects.queueRotationIndicators();
	}

	public float getParticleTargetRadius() {
		return .65f;
	}

	public float getParticleInitialRadius() {
		return .75f;
	}

}
