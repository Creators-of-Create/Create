package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.components.actors.RotatingActorModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapInstancedModel;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import com.simibubi.create.foundation.render.backend.instancing.impl.TransformedInstancedModel;
import net.minecraft.util.math.BlockPos;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    @Override
    public void registerMaterials() {
        materials.put(RenderMaterials.MODELS, new RenderMaterial<>(this, AllProgramSpecs.C_MODEL, TransformedInstancedModel::new));

        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.C_BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.C_ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.C_FLAPS, FlapInstancedModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(this, AllProgramSpecs.C_ACTOR, RotatingActorModel::new));
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return BlockPos.ZERO;
    }
}

