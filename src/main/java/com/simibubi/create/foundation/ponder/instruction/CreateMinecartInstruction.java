package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.element.MinecartElement;

import net.minecraft.core.Direction;

public class CreateMinecartInstruction extends FadeIntoSceneInstruction<MinecartElement> {

	public CreateMinecartInstruction(int fadeInTicks, Direction fadeInFrom, MinecartElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}
	
	@Override
	protected Class<MinecartElement> getElementClass() {
		return MinecartElement.class;
	}

}
