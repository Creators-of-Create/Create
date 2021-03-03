package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.components.actors.RotatingActorModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapInstancedModel;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import net.minecraft.util.math.BlockPos;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.CONTRAPTION_BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.CONTRAPTION_ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.CONTRAPTION_FLAPS, FlapInstancedModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(this, AllProgramSpecs.CONTRAPTION_ACTOR, RotatingActorModel::new));
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return BlockPos.ZERO;
    }
}

