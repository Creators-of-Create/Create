package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Select;

public class ShowCompleteSchematicInstruction extends PonderInstruction {

	@Override
	public void tick(PonderScene scene) {
		scene.addElement(Select.everything(scene.getBounds())
			.asElement());
	}

	@Override
	public boolean isComplete() {
		return true;
	}

}
