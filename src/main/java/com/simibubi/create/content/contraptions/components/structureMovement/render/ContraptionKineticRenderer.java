package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.components.actors.RotatingActorModel;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_ACTOR, RotatingActorModel::new));
    }
}
