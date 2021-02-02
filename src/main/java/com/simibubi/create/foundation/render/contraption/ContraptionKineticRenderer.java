package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.InstancedTileRenderer;
import com.simibubi.create.foundation.render.gl.shader.AllShaderPrograms;
import com.simibubi.create.foundation.render.instancing.BeltModel;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingModel;
import com.simibubi.create.foundation.render.instancing.actors.RotatingActorModel;

public class ContraptionKineticRenderer extends InstancedTileRenderer {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(AllShaderPrograms.CONTRAPTION_BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(AllShaderPrograms.CONTRAPTION_ROTATING, RotatingModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(AllShaderPrograms.CONTRAPTION_ACTOR, RotatingActorModel::new));
    }
}
