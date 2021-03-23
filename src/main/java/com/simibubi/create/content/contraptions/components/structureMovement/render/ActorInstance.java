package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.world.LightType;

public abstract class ActorInstance {
    protected final ContraptionKineticRenderer modelManager;
    protected final MovementContext context;

    public ActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        this.modelManager = modelManager;
        this.context = context;
    }

    public void tick() { }

    public void beginFrame() { }

    protected int localBlockLight() {
        return modelManager.getContraption().renderWorld.getLightLevel(LightType.BLOCK, context.localPos);
    }
}
