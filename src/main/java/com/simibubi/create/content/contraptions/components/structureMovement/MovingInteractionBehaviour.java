package com.simibubi.create.content.contraptions.components.structureMovement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public abstract class MovingInteractionBehaviour {
	public MovingInteractionBehaviour () { }

	public boolean handlePlayerInteraction (PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		return true;
	}
}
