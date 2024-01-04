package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public abstract class ActorInstance {
    protected final VisualizationContext visualizationContext;
	protected final InstancerProvider instancerProvider;
	protected final BlockAndTintGetter simulationWorld;
	protected final MovementContext context;

    public ActorInstance(VisualizationContext visualizationContext, BlockAndTintGetter world, MovementContext context) {
        this.visualizationContext = visualizationContext;
		this.instancerProvider = visualizationContext.instancerProvider();
        this.simulationWorld = world;
        this.context = context;
    }

	public void tick() { }

    public void beginFrame() { }

    protected int localBlockLight() {
        return simulationWorld.getBrightness(LightLayer.BLOCK, context.localPos);
    }
}
