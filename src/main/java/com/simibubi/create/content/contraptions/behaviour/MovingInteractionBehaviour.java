package com.simibubi.create.content.contraptions.behaviour;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public abstract class MovingInteractionBehaviour {

	protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index,
		StructureBlockInfo info, MovementContext ctx) {
		contraptionEntity.getContraption().getActors().remove(index);
		contraptionEntity.getContraption().getActors().add(index, MutablePair.of(info, ctx));
		if (contraptionEntity.level.isClientSide)
			contraptionEntity.getContraption().deferInvalidate = true;
	}

	protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos,
		StructureBlockInfo info) {
		if (contraptionEntity.level.isClientSide())
			return;
		contraptionEntity.setBlock(pos, info);
	}

	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		return true;
	}

	public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {}

}
