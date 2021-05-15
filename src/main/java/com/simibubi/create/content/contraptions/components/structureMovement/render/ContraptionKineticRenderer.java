package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.RenderMaterial;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    protected ArrayList<ActorInstance> actors = new ArrayList<>();

    private final WeakReference<RenderedContraption> contraption;

    ContraptionKineticRenderer(RenderedContraption contraption) {
		super(ContraptionContext.INSTANCE);
		this.contraption = new WeakReference<>(contraption);
	}

    public void tick() {
        actors.forEach(ActorInstance::tick);
    }

    @Override
	public void beginFrame(ActiveRenderInfo info) {
		super.beginFrame(info);

		actors.forEach(ActorInstance::beginFrame);
	}

    @Override
    protected boolean shouldTick(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
        return true;
    }

    @Nullable
    public ActorInstance createActor(Pair<Template.BlockInfo, MovementContext> actor) {
        Template.BlockInfo blockInfo = actor.getLeft();
        MovementContext context = actor.getRight();

        MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);

        if (movementBehaviour != null && movementBehaviour.hasSpecialInstancedRendering()) {
            ActorInstance instance = movementBehaviour.createInstance(this, context);

            actors.add(instance);

            return instance;
        }

        return null;
    }

    public RenderMaterial<?, ActorData> getActorMaterial() {
		return getMaterial(AllMaterialSpecs.ACTORS);
	}

    public RenderedContraption getContraption() {
        return contraption.get();
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return BlockPos.ZERO;
    }
}

