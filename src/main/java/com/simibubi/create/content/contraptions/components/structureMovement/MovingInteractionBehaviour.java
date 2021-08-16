package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import org.apache.commons.lang3.tuple.MutablePair;

public abstract class MovingInteractionBehaviour {
	public MovingInteractionBehaviour () { }

	protected void setContraptionActorData (AbstractContraptionEntity contraptionEntity, int index, BlockInfo info, MovementContext ctx) {
		contraptionEntity.contraption.actors.remove(index);
		contraptionEntity.contraption.actors.add(index, MutablePair.of(info, ctx));
		// mark contraption to re-render because we changed actor data
		ContraptionRenderDispatcher.invalidate(contraptionEntity.contraption);
	}

	protected void setContraptionBlockData (AbstractContraptionEntity contraptionEntity, BlockPos pos, BlockInfo info) {
		contraptionEntity.contraption.blocks.put(pos, info);
		// mark contraption to re-render because we changed block data
		ContraptionRenderDispatcher.invalidate(contraptionEntity.contraption);
	}

	public boolean handlePlayerInteraction (PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		return true;
	}
}
