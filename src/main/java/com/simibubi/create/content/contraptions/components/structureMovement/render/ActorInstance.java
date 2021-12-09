package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.api.MaterialManager;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.world.level.LightLayer;

public abstract class ActorInstance {
    protected final MaterialManager materialManager;
	protected final PlacementSimulationWorld simulationWorld;
	protected final MovementContext context;

    public ActorInstance(MaterialManager materialManager, PlacementSimulationWorld world, MovementContext context) {
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
