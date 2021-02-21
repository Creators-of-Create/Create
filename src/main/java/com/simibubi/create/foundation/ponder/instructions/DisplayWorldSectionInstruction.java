package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

import net.minecraft.util.Direction;

public class DisplayWorldSectionInstruction extends FadeIntoSceneInstruction<WorldSectionElement> {

	private Selection initialSelection;
	private boolean mergeToBase;

	public DisplayWorldSectionInstruction(int fadeInTicks, Direction fadeInFrom, Selection selection, boolean mergeToBase) {
		super(fadeInTicks, fadeInFrom, new WorldSectionElement(selection));
		initialSelection = selection;
		this.mergeToBase = mergeToBase;
	}
	
	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		element.set(initialSelection);
		element.setVisible(true);
	}
	
	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (remainingTicks > 0)
			return;
		if (!mergeToBase)
			return;
		element.mergeOnto(scene.getBaseWorldSection());
	}

	@Override
	protected Class<WorldSectionElement> getElementClass() {
		return WorldSectionElement.class;
	}
	
}
