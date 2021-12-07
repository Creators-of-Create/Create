package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.material.instancing.InstancedMaterialGroup;
import com.jozufozu.flywheel.backend.material.instancing.InstancingEngine;

public class ContraptionGroup<P extends ContraptionProgram> extends InstancedMaterialGroup<P> {

	private final RenderedContraption contraption;

	public ContraptionGroup(RenderedContraption contraption, InstancingEngine<P> owner) {
		super(owner);

		this.contraption = contraption;
	}

	@Override
	public void setup(P program) {
		contraption.setup(program);
	}

	public static <P extends ContraptionProgram> InstancingEngine.GroupFactory<P> forContraption(RenderedContraption c) {
		return (materialManager) -> new ContraptionGroup<>(c, materialManager);
	}
}
