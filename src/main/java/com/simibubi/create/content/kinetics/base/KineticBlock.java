package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

public abstract class KineticBlock extends Block implements IRotate {

	public KineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		// onBlockAdded is useless for init, as sometimes the BE gets re-instantiated

		// however, if a block change occurs that does not change kinetic connections,
		// we can prevent a major re-propagation here

		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		if (blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
			kineticBlockEntity.preventSpeedUpdate = 0;

			if (oldState.getBlock() != state.getBlock())
				return;
			if (state.hasBlockEntity() != oldState.hasBlockEntity())
				return;
			if (!areStatesKineticallyEquivalent(oldState, state))
				return;

			kineticBlockEntity.preventSpeedUpdate = 2;
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving) {
		IBE.onRemove(pState, pLevel, pPos, Blocks.AIR.defaultBlockState());
		super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pIsMoving);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		if (oldState.getBlock() != newState.getBlock())
			return false;
		return getRotationAxis(newState) == getRotationAxis(oldState);
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags,
											  int count) {
		if (worldIn.isClientSide())
			return;

		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		if (!(blockEntity instanceof KineticBlockEntity kbe))
			return;

		if (kbe.preventSpeedUpdate > 0)
			return;

		// Remove previous information when block is added
		kbe.warnOfMovement();
		kbe.clearKineticInformation();
		kbe.updateSpeed = true;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		AdvancementBehaviour.setPlacedBy(worldIn, pos, placer);
		if (worldIn.isClientSide())
			return;

		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		if (!(blockEntity instanceof KineticBlockEntity kbe))
			return;

		kbe.effects.queueRotationIndicators();
	}

	public float getParticleTargetRadius() {
		return .65f;
	}

	public float getParticleInitialRadius() {
		return .75f;
	}

}
