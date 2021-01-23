package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.FastKineticRenderer;
import com.simibubi.create.foundation.render.instancing.BeltBuffer;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingBuffer;
import com.simibubi.create.foundation.render.instancing.actors.RotatingActorBuffer;
import com.simibubi.create.foundation.render.shader.Shader;

public class ContraptionKineticRenderer extends FastKineticRenderer {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.CONTRAPTION_BELT, BeltBuffer::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.CONTRAPTION_ROTATING, RotatingBuffer::new));
        materials.put(KineticRenderMaterials.ACTORS, new RenderMaterial<>(Shader.CONTRAPTION_ACTOR, RotatingActorBuffer::new));
    }

    @Override
    protected void prepareFrame() {}
}
