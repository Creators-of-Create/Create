package com.simibubi.create.content.contraptions.actors.trainControls;

import java.util.UUID;

import com.google.common.base.Objects;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ControlsInteractionBehaviour extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(activeHand)))
			return false;

		UUID currentlyControlling = contraptionEntity.getControllingPlayer()
			.orElse(null);

		if (currentlyControlling != null) {
			// If the same player is already controlling, don't stop controlling - just continue
			if (Objects.equal(currentlyControlling, player.getUUID()))
				return true;

			// Different player is trying to control, stop current control first
			contraptionEntity.stopControlling(localPos);
		}

		if (!contraptionEntity.startControlling(localPos, player))
			return false;

		contraptionEntity.setControllingPlayer(player.getUUID());
		if (player.level().isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> ControlsHandler.startControlling(contraptionEntity, localPos));
		return true;
	}

}
