package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

import net.minecraft.util.Direction;

public class DisplayWorldSectionInstruction extends FadeIntoSceneInstruction<WorldSectionElement> {

	public DisplayWorldSectionInstruction(int fadeInTicks, Direction fadeInFrom, WorldSectionElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}

}
