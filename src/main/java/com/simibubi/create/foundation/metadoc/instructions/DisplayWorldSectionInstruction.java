package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.elements.WorldSectionElement;

import net.minecraft.util.Direction;

public class DisplayWorldSectionInstruction extends FadeIntoSceneInstruction<WorldSectionElement> {

	public DisplayWorldSectionInstruction(int fadeInTicks, Direction fadeInFrom, WorldSectionElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}

}
