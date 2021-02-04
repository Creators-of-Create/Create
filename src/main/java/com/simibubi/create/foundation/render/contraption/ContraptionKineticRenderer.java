package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.instancing.BeltModel;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingModel;
import com.simibubi.create.foundation.render.instancing.actors.RotatingActorModel;

public class ContraptionKineticRenderer extends InstancedTileRenderer<ContraptionProgram> {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_ROTATING, RotatingModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(AllProgramSpecs.CONTRAPTION_ACTOR, RotatingActorModel::new));
    }
}
