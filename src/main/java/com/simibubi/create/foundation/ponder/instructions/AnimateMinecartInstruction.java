package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.elements.MinecartElement;

import net.minecraft.util.math.vector.Vector3d;

public class AnimateMinecartInstruction extends AnimateElementInstruction<MinecartElement> {

	public static AnimateMinecartInstruction rotate(ElementLink<MinecartElement> link, float rotation, int ticks) {
		return new AnimateMinecartInstruction(link, new Vector3d(0, rotation, 0), ticks,
			(wse, v) -> wse.setRotation((float) v.y, ticks == 0), MinecartElement::getRotation);
	}

	public static AnimateMinecartInstruction move(ElementLink<MinecartElement> link, Vector3d offset, int ticks) {
		return new AnimateMinecartInstruction(link, offset, ticks, (wse, v) -> wse.setPositionOffset(v, ticks == 0),
			MinecartElement::getPositionOffset);
	}

	protected AnimateMinecartInstruction(ElementLink<MinecartElement> link, Vector3d totalDelta, int ticks,
		BiConsumer<MinecartElement, Vector3d> setter, Function<MinecartElement, Vector3d> getter) {
		super(link, totalDelta, ticks, setter, getter);
	}

}
