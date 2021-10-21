package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public abstract class MovingInteractionBehaviour {

	public MovingInteractionBehaviour() {}

	protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index, BlockInfo info,
		MovementContext ctx) {
		contraptionEntity.contraption.actors.remove(index);
		contraptionEntity.contraption.actors.add(index, MutablePair.of(info, ctx));
		ContraptionRenderDispatcher.invalidate(contraptionEntity.contraption);
	}

	protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos, BlockInfo info) {
		contraptionEntity.contraption.blocks.put(pos, info);
		ContraptionRenderDispatcher.invalidate(contraptionEntity.contraption);
	}

	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		return true;
	}

}
