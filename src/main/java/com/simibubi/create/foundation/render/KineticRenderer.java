package com.simibubi.create.foundation.render;

import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.instancing.*;

public class KineticRenderer extends InstancedTileRenderer<BasicProgram> {
    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(AllProgramSpecs.BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(AllProgramSpecs.ROTATING, RotatingInstancedModel::new));
    }
}
