package com.simibubi.create.api.behaviour.interaction;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * MovingInteractionBehaviors define behavior of blocks on contraptions
 * when interacted with by players or collided with by entities.
 */
public abstract class MovingInteractionBehaviour {
	public static final SimpleRegistry<Block, MovingInteractionBehaviour> REGISTRY = SimpleRegistry.create();

	/**
	 * Creates a consumer that will register a behavior to a block. Useful for Registrate.
	 */
	public static <B extends Block> NonNullConsumer<? super B> interactionBehaviour(MovingInteractionBehaviour behaviour) {
		return b -> REGISTRY.register(b, behaviour);
	}

	protected void setContraptionActorData(AbstractContraptionEntity contraptionEntity, int index,
										   StructureBlockInfo info, MovementContext ctx) {
		contraptionEntity.getContraption().getActors().remove(index);
		contraptionEntity.getContraption().getActors().add(index, MutablePair.of(info, ctx));
		if (contraptionEntity.level().isClientSide)
			contraptionEntity.getContraption()
				.invalidateClientContraptionChildren();
	}

	protected void setContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos pos,
		StructureBlockInfo info) {
		if (contraptionEntity.level().isClientSide())
			return;
		contraptionEntity.setBlock(pos, info);
	}

	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		return true;
	}

	public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {}

}
