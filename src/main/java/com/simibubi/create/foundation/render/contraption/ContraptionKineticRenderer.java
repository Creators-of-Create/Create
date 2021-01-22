package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.FastKineticRenderer;
import com.simibubi.create.foundation.render.instancing.BeltBuffer;
import com.simibubi.create.foundation.render.instancing.KineticRenderMaterials;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingBuffer;
import com.simibubi.create.foundation.render.shader.Shader;
import com.simibubi.create.foundation.render.shader.ShaderCallback;
import com.simibubi.create.foundation.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;

public class ContraptionKineticRenderer extends FastKineticRenderer {

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.CONTRAPTION_BELT, BeltBuffer::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.CONTRAPTION_ROTATING, RotatingBuffer::new));
    }

    @Override
    protected void prepareFrame() {}
}
