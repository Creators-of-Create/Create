package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.core.materials.BasicAttributes;
import com.jozufozu.flywheel.backend.core.materials.OrientedAttributes;
import com.jozufozu.flywheel.backend.core.materials.TransformAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.simibubi.create.content.contraptions.base.KineticAttributes;
import com.simibubi.create.content.contraptions.base.RotatingAttributes;
import com.simibubi.create.content.contraptions.components.actors.ActorVertexAttributes;
import com.simibubi.create.content.contraptions.relays.belt.BeltAttributes;
import com.simibubi.create.content.logistics.block.FlapAttributes;

public class AllInstanceFormats {

	public static final VertexFormat MODEL = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.build();
	public static final VertexFormat ORIENTED = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.build();
	public static VertexFormat ROTATING = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.build();
	public static VertexFormat ACTOR = VertexFormat.builder()
			.addAttributes(ActorVertexAttributes.class)
			.build();
	public static VertexFormat BELT = VertexFormat.builder()
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.build();
	public static VertexFormat FLAP = VertexFormat.builder()
			.addAttributes(FlapAttributes.class)
			.build();
}
