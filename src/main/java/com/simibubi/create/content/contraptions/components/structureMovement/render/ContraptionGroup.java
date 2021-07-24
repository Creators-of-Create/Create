package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.material.MaterialGroup;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.state.IRenderState;

public class ContraptionGroup<P extends ContraptionProgram> extends MaterialGroup<P> {

	private final RenderedContraption contraption;

	public ContraptionGroup(RenderedContraption contraption, MaterialManager<P> owner, IRenderState state) {
		super(owner, state);

		this.contraption = contraption;
	}

	@Override
	public void setup(P program) {
		contraption.setup(program);
	}

	public static <P extends ContraptionProgram> MaterialManager.GroupFactory<P> forContraption(RenderedContraption c) {
		return (materialManager, state) -> new ContraptionGroup<>(c, materialManager, state);
	}
}
