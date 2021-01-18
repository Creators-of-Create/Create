package com.simibubi.create.foundation.render.light;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.RenderedContraption;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import com.simibubi.create.foundation.utility.outliner.Outline;

public class LightVolumeDebugger {
    public static void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
        ContraptionRenderDispatcher.renderers.values()
                                             .stream()
                                             .map(r -> r.getLighter().lightVolume.getBox())
                                             .map(volume -> new AABBOutline(GridAlignedBB.toAABB(volume)))
                                             .forEach(outline -> outline.render(ms, buffer));
    }
}
