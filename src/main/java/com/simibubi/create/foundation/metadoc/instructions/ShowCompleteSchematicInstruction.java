package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocInstruction;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.Select;

public class ShowCompleteSchematicInstruction extends MetaDocInstruction {

	@Override
	public void tick(MetaDocScene scene) {
		scene.addElement(Select.everything(scene)
			.asElement());
	}

	@Override
	public boolean isComplete() {
		return true;
	}

}
