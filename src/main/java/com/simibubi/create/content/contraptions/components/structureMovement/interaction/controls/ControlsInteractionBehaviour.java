package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ControlsInteractionBehaviour extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(activeHand)))
			return false;
		if (player.level.isClientSide)
			ControlsHandler.controllerClicked(contraptionEntity, localPos, player);
		return true;
	}

}
