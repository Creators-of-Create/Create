package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.components.actors.ContraptionActorData;
import com.simibubi.create.content.contraptions.components.actors.RotatingActorModel;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapInstancedModel;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;

import com.simibubi.create.foundation.render.backend.instancing.impl.BasicInstancedModel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    protected ArrayList<ActorInstance> actors = new ArrayList<>();

    public final RenderedContraption contraption;

    ContraptionKineticRenderer(RenderedContraption contraption) {
        this.contraption = contraption;
    }

    @Override
    public void registerMaterials() {
        materials.put(RenderMaterials.MODELS, new RenderMaterial<>(this, AllProgramSpecs.C_MODEL, BasicInstancedModel::new));

        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.C_BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.C_ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.C_FLAPS, FlapInstancedModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(this, AllProgramSpecs.C_ACTOR, RotatingActorModel::new));
    }


    @Override
    public void beginFrame(double cameraX, double cameraY, double cameraZ) {
        super.beginFrame(cameraX, cameraY, cameraZ);

        actors.forEach(ActorInstance::tick);
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

    public RenderMaterial<?, InstancedModel<ContraptionActorData>> getActorMaterial() {
        return getMaterial(KineticRenderMaterials.ACTORS);
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return BlockPos.ZERO;
    }
}

