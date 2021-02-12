package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocInstruction;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.Select;
import com.simibubi.create.foundation.metadoc.elements.WorldSectionElement;

public abstract class WorldModifyInstruction extends MetaDocInstruction {

	private Select selection;

	public WorldModifyInstruction(Select selection) {
		this.selection = selection;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(MetaDocScene scene) {
		runModification(selection, scene);
		if (needsRedraw())
			scene.forEach(WorldSectionElement.class, wse -> wse.queueRedraw(scene.getWorld()));
	}

	protected abstract void runModification(Select selection, MetaDocScene scene);

	protected abstract boolean needsRedraw();

}
