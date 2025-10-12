package com.simibubi.create.api.behaviour.movement;

import com.simibubi.create.api.behaviour.movement.ContraptionHandoffContainer.ContraptionHandoffContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.world.level.LevelAccessor;

/**
 * A {@link MovementBehaviour} which allows actors to 
 * define custom assembly and disassembly behaviours.
 */
public interface ListeningMovementBehaviour extends MovementBehaviour {

    /**
     * Called when a contraption has been initialized that had previously
     * destroyed a Block registered with this behaviour. 
     * At the time of invocation, any Blocks or BlockEntities that may 
     * have existed will have already been removed, so the BlockEntity 
     * instance in the container is stale.
     * @param world 
     * @param ace 
     * @param contraption
     * @param ctx {@link ContraptionHandoffContext} containing a stale BlockEntity reference
     */
    public default void onAddedToContraption(LevelAccessor world, AbstractContraptionEntity ace, Contraption contraption, ContraptionHandoffContext ctx) {}

    /**
     * Called as an {@link AbstractContraptionEntity} is disassembling.
     * This method is called after all blocks have been placed back into the world
     * and the contraption entity has been removed.
     * @param world 
     * @param contraption caller
     * @param ctx {@link ContraptionHandoffContext} containing the now re-instantiated BlockEntity reference
     */
    public default void onRemovedFromContraption(LevelAccessor world, AbstractContraptionEntity ace, Contraption contraption, ContraptionHandoffContext ctx) {}
}
