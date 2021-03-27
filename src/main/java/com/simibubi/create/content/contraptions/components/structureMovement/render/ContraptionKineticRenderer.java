package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingModel;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.components.actors.ActorModel;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapModel;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import com.simibubi.create.foundation.render.backend.instancing.impl.OrientedModel;
import com.simibubi.create.foundation.render.backend.instancing.impl.TransformedModel;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    protected ArrayList<ActorInstance> actors = new ArrayList<>();

    private final WeakReference<RenderedContraption> contraption;

    ContraptionKineticRenderer(RenderedContraption contraption) {
        this.contraption = new WeakReference<>(contraption);
    }

    @Override
    public void registerMaterials() {
        materials.put(RenderMaterials.TRANSFORMED, new RenderMaterial<>(this, AllProgramSpecs.C_MODEL, TransformedModel::new));
        materials.put(RenderMaterials.ORIENTED, new RenderMaterial<>(this, AllProgramSpecs.C_ORIENTED, OrientedModel::new));

        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.C_BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.C_ROTATING, RotatingModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.C_FLAPS, FlapModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(this, AllProgramSpecs.C_ACTOR, ActorModel::new));
    }

    public void tick() {
        actors.forEach(ActorInstance::tick);
    }

    @Override
    public void beginFrame(ActiveRenderInfo info, double cameraX, double cameraY, double cameraZ) {
        super.beginFrame(info, cameraX, cameraY, cameraZ);

        actors.forEach(ActorInstance::beginFrame);
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

    public RenderMaterial<?, InstancedModel<ActorData>> getActorMaterial() {
        return getMaterial(KineticRenderMaterials.ACTORS);
    }

    public RenderedContraption getContraption() {
        return contraption.get();
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return BlockPos.ZERO;
    }
}

