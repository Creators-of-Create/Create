package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public abstract class MovingInteractionBehaviour {

	public MovingInteractionBehaviour() {}

	protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index, BlockInfo info,
		MovementContext ctx) {
		contraptionEntity.contraption.actors.remove(index);
		contraptionEntity.contraption.actors.add(index, MutablePair.of(info, ctx));
		if (contraptionEntity.level.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> invalidate(contraptionEntity.contraption));
	}

	protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos, BlockInfo info) {
		contraptionEntity.contraption.blocks.put(pos, info);
		if (contraptionEntity.level.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> invalidate(contraptionEntity.contraption));
	}

	@OnlyIn(Dist.CLIENT)
	protected void invalidate(Contraption contraption) {
		ContraptionRenderDispatcher.invalidate(contraption);
	}

	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		return true;
	}

}
