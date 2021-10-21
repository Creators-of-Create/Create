package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public abstract class SimpleBlockMovingInteraction extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		Contraption contraption = contraptionEntity.getContraption();
		BlockInfo info = contraption.getBlocks()
			.get(localPos);

		BlockState newState = handle(player, contraption, localPos, info.state);
		if (info.state == newState)
			return false;

		setContraptionBlockData(contraptionEntity, localPos, new BlockInfo(info.pos, newState, info.nbt));
		if (updateColliders())
			contraption.invalidateColliders();
		return true;
	}

	protected boolean updateColliders() {
		return false;
	}

	protected void playSound(PlayerEntity player, SoundEvent sound, float pitch) {
		player.level.playSound(player, player.blockPosition(), sound, SoundCategory.BLOCKS, 0.3f, pitch);
	}

	protected abstract BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos,
		BlockState currentState);

}
