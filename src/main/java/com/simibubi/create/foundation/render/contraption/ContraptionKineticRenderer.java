package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.gl.shader.Shader;
import com.simibubi.create.foundation.render.instancing.BeltModel;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingModel;
import com.simibubi.create.foundation.render.instancing.actors.RotatingActorModel;

public class ContraptionKineticRenderer extends InstancedTileRenderDispatcher {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.CONTRAPTION_BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.CONTRAPTION_ROTATING, RotatingModel::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(Shader.CONTRAPTION_ACTOR, RotatingActorModel::new));
    }
}
