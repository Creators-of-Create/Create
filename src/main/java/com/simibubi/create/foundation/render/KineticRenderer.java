package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.BasicProgram;
import com.simibubi.create.foundation.render.instancing.*;

public class KineticRenderer extends InstancedTileRenderer<BasicProgram> {
    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(AllProgramSpecs.BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(AllProgramSpecs.ROTATING, RotatingModel::new));
    }
}
