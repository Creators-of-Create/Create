package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.material.MaterialGroupImpl;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;
import com.jozufozu.flywheel.backend.state.IRenderState;

public class ContraptionGroup<P extends ContraptionProgram> extends MaterialGroupImpl<P> {

	private final RenderedContraption contraption;

	public ContraptionGroup(RenderedContraption contraption, MaterialManagerImpl<P> owner, IRenderState state) {
		super(owner, state);

		this.contraption = contraption;
	}

	@Override
	public void setup(P program) {
		contraption.setup(program);
	}

	public static <P extends ContraptionProgram> MaterialManagerImpl.GroupFactory<P> forContraption(RenderedContraption c) {
		return (materialManager, state) -> new ContraptionGroup<>(c, materialManager, state);
	}
}
