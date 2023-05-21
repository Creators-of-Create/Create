package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.level.LightLayer;

public abstract class ActorInstance {
    protected final MaterialManager materialManager;
	protected final VirtualRenderWorld simulationWorld;
	protected final MovementContext context;

    public ActorInstance(MaterialManager materialManager, VirtualRenderWorld world, MovementContext context) {
        this.materialManager = materialManager;
        this.simulationWorld = world;
        this.context = context;
    }

	public void tick() { }

    public void beginFrame() { }

    protected int localBlockLight() {
        return simulationWorld.getBrightness(LightLayer.BLOCK, context.localPos);
    }
}
