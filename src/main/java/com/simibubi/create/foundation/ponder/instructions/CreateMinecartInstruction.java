package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.elements.MinecartElement;

import net.minecraft.util.Direction;

public class CreateMinecartInstruction extends FadeIntoSceneInstruction<MinecartElement> {

	public CreateMinecartInstruction(int fadeInTicks, Direction fadeInFrom, MinecartElement element) {
		super(fadeInTicks, fadeInFrom, element);
	}
	
	@Override
	protected Class<MinecartElement> getElementClass() {
		return MinecartElement.class;
	}

}
