package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class DoorMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound =
			currentState.getValue(DoorBlock.OPEN) ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN;

		BlockPos otherPos = currentState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		BlockInfo info = contraption.getBlocks()
			.get(otherPos);
		if (info.state.hasProperty(DoorBlock.OPEN))
			setContraptionBlockData(contraption.entity, otherPos,
				new BlockInfo(info.pos, info.state.cycle(DoorBlock.OPEN), info.nbt));

		float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(DoorBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
