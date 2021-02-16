package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.elements.ParrotElement;

import net.minecraft.util.Direction;

public class CreateParrotInstruction extends FadeIntoSceneInstruction<ParrotElement> {

	public CreateParrotInstruction(int fadeInTicks, Direction fadeInFrom, ParrotElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}

}
