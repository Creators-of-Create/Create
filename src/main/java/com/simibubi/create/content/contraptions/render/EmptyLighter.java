package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.lib.box.MutableBox;
import com.simibubi.create.content.contraptions.Contraption;

// so other contraptions don't crash before they have a lighter
public class EmptyLighter extends ContraptionLighter<Contraption> {
	public EmptyLighter(Contraption contraption) {
		super(contraption);
	}

	@Override
	public MutableBox getContraptionBounds() {
		return new MutableBox(0, 0, 0, 1, 1, 1);
	}
}
