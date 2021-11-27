package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.element.ParrotElement;

import net.minecraft.core.Direction;

public class CreateParrotInstruction extends FadeIntoSceneInstruction<ParrotElement> {

	public CreateParrotInstruction(int fadeInTicks, Direction fadeInFrom, ParrotElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}
	
	@Override
	protected Class<ParrotElement> getElementClass() {
		return ParrotElement.class;
	}

}
