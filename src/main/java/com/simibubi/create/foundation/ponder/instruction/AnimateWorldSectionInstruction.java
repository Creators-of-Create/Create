package com.simibubi.create.foundation.ponder.instruction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;

import net.minecraft.world.phys.Vec3;

public class AnimateWorldSectionInstruction extends AnimateElementInstruction<WorldSectionElement> {

	public static AnimateWorldSectionInstruction rotate(ElementLink<WorldSectionElement> link, Vec3 rotation,
		int ticks) {
		return new AnimateWorldSectionInstruction(link, rotation, ticks,
			(wse, v) -> wse.setAnimatedRotation(v, ticks == 0), WorldSectionElement::getAnimatedRotation);
	}

	public static AnimateWorldSectionInstruction move(ElementLink<WorldSectionElement> link, Vec3 offset, int ticks) {
		return new AnimateWorldSectionInstruction(link, offset, ticks, (wse, v) -> wse.setAnimatedOffset(v, ticks == 0),
			WorldSectionElement::getAnimatedOffset);
	}

	protected AnimateWorldSectionInstruction(ElementLink<WorldSectionElement> link, Vec3 totalDelta, int ticks,
		BiConsumer<WorldSectionElement, Vec3> setter, Function<WorldSectionElement, Vec3> getter) {
		super(link, totalDelta, ticks, setter, getter);
	}

}
