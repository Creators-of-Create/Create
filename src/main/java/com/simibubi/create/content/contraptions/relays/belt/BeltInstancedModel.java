package com.simibubi.create.content.contraptions.relays.belt;

import com.jozufozu.flywheel.backend.core.materials.BasicAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.content.contraptions.base.KineticAttributes;

import net.minecraft.client.renderer.BufferBuilder;

public class BeltInstancedModel extends InstancedModel<BeltData> {
    public static VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
            .addAttributes(KineticAttributes.class)
            .addAttributes(BeltAttributes.class)
            .build();

	public BeltInstancedModel(InstancedTileRenderer<?> renderer, BufferBuilder buf) {
        super(renderer, buf);
    }

	@Override
	protected BeltData newInstance() {
        return new BeltData(this);
    }

    @Override
	protected VertexFormat getInstanceFormat() {
        return FORMAT;
    }

}
