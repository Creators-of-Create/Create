package com.simibubi.create.foundation.ponder.instruction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.element.ParrotElement;

import net.minecraft.world.phys.Vec3;

public class AnimateParrotInstruction extends AnimateElementInstruction<ParrotElement> {

	public static AnimateParrotInstruction rotate(ElementLink<ParrotElement> link, Vec3 rotation, int ticks) {
		return new AnimateParrotInstruction(link, rotation, ticks, (wse, v) -> wse.setRotation(v, ticks == 0),
			ParrotElement::getRotation);
	}

	public static AnimateParrotInstruction move(ElementLink<ParrotElement> link, Vec3 offset, int ticks) {
		return new AnimateParrotInstruction(link, offset, ticks, (wse, v) -> wse.setPositionOffset(v, ticks == 0),
			ParrotElement::getPositionOffset);
	}

	protected AnimateParrotInstruction(ElementLink<ParrotElement> link, Vec3 totalDelta, int ticks,
		BiConsumer<ParrotElement, Vec3> setter, Function<ParrotElement, Vec3> getter) {
		super(link, totalDelta, ticks, setter, getter);
	}

}
