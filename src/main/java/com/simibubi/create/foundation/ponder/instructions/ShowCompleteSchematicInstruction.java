package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

public class ShowCompleteSchematicInstruction extends PonderInstruction {

	@Override
	public void tick(PonderScene scene) {
		scene.addElement(new WorldSectionElement(scene.getSceneBuildingUtil().select.everywhere()));
	}

	@Override
	public boolean isComplete() {
		return true;
	}

}
