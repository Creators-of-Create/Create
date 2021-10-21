package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class LeverMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
		playSound(player, SoundEvents.LEVER_CLICK, currentState.getValue(LeverBlock.POWERED) ? 0.5f : 0.6f);
		return currentState.cycle(LeverBlock.POWERED);
	}

}
