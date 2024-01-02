package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public abstract class ActorInstance {
    protected final VisualizationContext materialManager;
	protected final BlockAndTintGetter simulationWorld;
	protected final MovementContext context;

    public ActorInstance(VisualizationContext materialManager, BlockAndTintGetter world, MovementContext context) {
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
