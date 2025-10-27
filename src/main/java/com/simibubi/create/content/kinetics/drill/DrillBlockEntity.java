package com.simibubi.create.content.kinetics.drill;

import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.drill.CobbleGenOptimisation.CobbleGenBlockConfiguration;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class DrillBlockEntity extends BlockBreakingKineticBlockEntity {

	private CobbleGenBlockConfiguration currentConfig;
	private BlockState currentOutput;

	public DrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		currentOutput = Blocks.AIR.defaultBlockState();
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getBlockPos().relative(getBlockState().getValue(DrillBlock.FACING));
	}

	@Override
	public void onBlockBroken(BlockState stateToBreak) {
		if (!optimiseCobbleGen(stateToBreak))
			super.onBlockBroken(stateToBreak);
	}

	public boolean optimiseCobbleGen(BlockState stateToBreak) {
		DirectBeltInputBehaviour inv =
			BlockEntityBehaviour.get(level, breakingPos.below(), DirectBeltInputBehaviour.TYPE);
		BlockEntity blockEntityBelow = level.getBlockEntity(breakingPos.below());
		BlockEntity blockEntityAbove = level.getBlockEntity(breakingPos.above());

		if (inv == null && !(blockEntityBelow instanceof HopperBlockEntity)
			&& !(blockEntityAbove instanceof ChuteBlockEntity chute && chute.getItemMotion() > 0))
			return false;

		CobbleGenBlockConfiguration config =
			CobbleGenOptimisation.getConfig(level, worldPosition, getBlockState().getValue(DrillBlock.FACING));
		if (config == null)
			return false;
		if (!(level instanceof ServerLevel sl))
			return false;

		BlockPos breakingPos = getBreakingPos();
		if (!config.equals(currentConfig)) {
			currentConfig = config;
			currentOutput = CobbleGenOptimisation.determineOutput(sl, breakingPos, config);
		}

		if (currentOutput.isAir() || !currentOutput.equals(stateToBreak))
			return false;

		if (inv != null)
			for (ItemStack stack : Block.getDrops(stateToBreak, sl, breakingPos, null))
				inv.handleInsertion(stack, Direction.UP, false);
		else if (blockEntityBelow instanceof HopperBlockEntity hbe) {
			IItemHandler handler = level.getCapability(ItemHandler.BLOCK, hbe.getBlockPos(), null);
			if (handler != null)
				for (ItemStack stack : Block.getDrops(stateToBreak, sl, breakingPos, null))
					ItemHandlerHelper.insertItemStacked(handler, stack, false);
		} else if (blockEntityAbove instanceof ChuteBlockEntity chute && chute.getItemMotion() > 0) {
			for (ItemStack stack : Block.getDrops(stateToBreak, sl, breakingPos, null))
				if (chute.getItem()
					.isEmpty())
					chute.setItem(stack, 0);
		}

		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, breakingPos, Block.getId(stateToBreak));
		return true;
	}

}
