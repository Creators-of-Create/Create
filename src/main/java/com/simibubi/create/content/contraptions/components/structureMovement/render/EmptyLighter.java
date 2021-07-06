package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;

// so other contraptions don't crash before they have a lighter
public class EmptyLighter extends ContraptionLighter<Contraption> {
	public EmptyLighter(Contraption contraption) {
		super(contraption);
	}

	@Override
	public GridAlignedBB getContraptionBounds() {
		return new GridAlignedBB(0, 0, 0, 1, 1, 1);
	}
}
