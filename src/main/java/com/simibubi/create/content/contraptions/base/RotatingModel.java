package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.core.materials.BasicAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;

import net.minecraft.client.renderer.BufferBuilder;

public class RotatingModel extends InstancedModel<RotatingData> {
    public static VertexFormat FORMAT = VertexFormat.builder()
            .addAttributes(BasicAttributes.class)
            .addAttributes(KineticAttributes.class)
            .addAttributes(RotatingAttributes.class)
            .build();

    public RotatingModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

    @Override
    protected RotatingData newInstance() {
        return new RotatingData(this);
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return FORMAT;
    }

}
