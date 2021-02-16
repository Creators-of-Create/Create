package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;

public class MarkAsFinishedInstruction extends PonderInstruction {

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(PonderScene scene) {
		scene.finished = true;
	}

}
