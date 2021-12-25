package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ContraptionInstanceManager extends TileInstanceManager {

    protected ArrayList<ActorInstance> actors = new ArrayList<>();

    private final PlacementSimulationWorld renderWorld;

    ContraptionInstanceManager(MaterialManager materialManager, PlacementSimulationWorld contraption) {
		super(materialManager);
		this.renderWorld = contraption;
	}

    public void tick() {
        actors.forEach(ActorInstance::tick);
    }

    @Override
	public void beginFrame(TaskEngine taskEngine, Camera info) {
		super.beginFrame(taskEngine, info);

		actors.forEach(ActorInstance::beginFrame);
	}

    @Override
	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}

    @Nullable
    public ActorInstance createActor(Pair<StructureBlockInfo, MovementContext> actor) {
    	StructureBlockInfo blockInfo = actor.getLeft();
        MovementContext context = actor.getRight();

        MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);

        if (movementBehaviour != null && movementBehaviour.hasSpecialInstancedRendering()) {
            ActorInstance instance = movementBehaviour.createInstance(materialManager, renderWorld, context);

            actors.add(instance);

            return instance;
        }

        return null;
    }
}

