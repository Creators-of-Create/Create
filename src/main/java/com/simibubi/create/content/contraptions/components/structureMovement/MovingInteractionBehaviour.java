package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.fml.DistExecutor;

public abstract class MovingInteractionBehaviour {

	public MovingInteractionBehaviour() {}

	protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index, StructureBlockInfo info,
		MovementContext ctx) {
		contraptionEntity.contraption.actors.remove(index);
		contraptionEntity.contraption.actors.add(index, MutablePair.of(info, ctx));
		if (contraptionEntity.level.isClientSide)
			DistExecutor.unsafeRunWhenOn(EnvType.CLIENT, () -> () -> invalidate(contraptionEntity.contraption));
	}

	protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos, StructureBlockInfo info) {
		contraptionEntity.contraption.blocks.put(pos, info);
		if (contraptionEntity.level.isClientSide)
			DistExecutor.unsafeRunWhenOn(EnvType.CLIENT, () -> () -> invalidate(contraptionEntity.contraption));
	}

	@Environment(EnvType.CLIENT)
	protected void invalidate(Contraption contraption) {
		ContraptionRenderDispatcher.invalidate(contraption);
	}

	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		return true;
	}

}
