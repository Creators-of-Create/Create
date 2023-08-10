package com.simibubi.create.content.contraptions.actors.trainControls;

import java.util.UUID;

import com.google.common.base.Objects;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;

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
			contraptionEntity.stopControlling(localPos);
			if (Objects.equal(currentlyControlling, player.getUUID()))
				return true;
		}

		if (!contraptionEntity.startControlling(localPos, player))
			return false;

		contraptionEntity.setControllingPlayer(player.getUUID());
		if (player.level.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> ControlsHandler.startControlling(contraptionEntity, localPos));
		return true;
	}

}
