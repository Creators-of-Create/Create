package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public abstract class ActorInstance {
    protected final ContraptionKineticRenderer modelManager;
    protected final MovementContext context;

    public ActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        this.modelManager = modelManager;
        this.context = context;
    }

    protected void tick() { }

    protected float getSpeed(Direction facing) {
        if (context.contraption.stalled)
            return 0;

        return !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()) ? context.getAnimationSpeed() : 0;
    }

    protected int localBlockLight() {
        return modelManager.contraption.renderWorld.getLightLevel(LightType.BLOCK, context.localPos);
    }
}
