package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

public abstract class WorldModifyInstruction extends PonderInstruction {

	private Select selection;

	public WorldModifyInstruction(Select selection) {
		this.selection = selection;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(PonderScene scene) {
		runModification(selection, scene);
		if (needsRedraw())
			scene.forEach(WorldSectionElement.class, wse -> wse.queueRedraw(scene.getWorld()));
	}

	protected abstract void runModification(Select selection, PonderScene scene);

	protected abstract boolean needsRedraw();

}
