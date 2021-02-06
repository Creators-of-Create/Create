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
		for (ToolType toolType : player.getHeldItemMainhand()
			.getToolTypes()) {
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

		// however, if a block change occurs that does not change kinetic connections,
		// we can prevent a major re-propagation here

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof KineticTileEntity) {
			KineticTileEntity kineticTileEntity = (KineticTileEntity) tileEntity;
			kineticTileEntity.preventSpeedUpdate = false;

			if (oldState.getBlock() != state.getBlock())
				return;
			if (state.hasTileEntity() != oldState.hasTileEntity())
				return;
			if (!areStatesKineticallyEquivalent(oldState, state))
				return;

			kineticTileEntity.preventSpeedUpdate = true;
		}
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

	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return getRotationAxis(newState) == getRotationAxis(oldState);
	}

	@Override
	public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

	@Override
	public void updateDiagonalNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags, int count) {
		if (worldIn.isRemote())
			return;

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;
		KineticTileEntity kte = (KineticTileEntity) tileEntity;

		if (kte.preventSpeedUpdate) {
			kte.preventSpeedUpdate = false;
			return;
		}

		// Remove previous information when block is added
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
