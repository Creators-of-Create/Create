package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public abstract class SimpleBlockMovingInteraction extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		Contraption contraption = contraptionEntity.getContraption();
		StructureBlockInfo info = contraption.getBlocks()
			.get(localPos);

		BlockState newState = handle(player, contraption, localPos, info.state);
		if (info.state == newState)
			return false;

		setContraptionBlockData(contraptionEntity, localPos, new StructureBlockInfo(info.pos, newState, info.nbt));
		if (updateColliders())
			contraption.invalidateColliders();
		return true;
	}

	protected boolean updateColliders() {
		return false;
	}

	protected void playSound(Player player, SoundEvent sound, float pitch) {
		player.level.playSound(player, player.blockPosition(), sound, SoundSource.BLOCKS, 0.3f, pitch);
	}

	protected abstract BlockState handle(Player player, Contraption contraption, BlockPos pos,
		BlockState currentState);

}
