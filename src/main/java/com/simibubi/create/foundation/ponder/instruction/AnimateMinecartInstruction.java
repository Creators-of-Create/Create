package com.simibubi.create.foundation.ponder.instruction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.element.MinecartElement;

import net.minecraft.world.phys.Vec3;

public class AnimateMinecartInstruction extends AnimateElementInstruction<MinecartElement> {

	public static AnimateMinecartInstruction rotate(ElementLink<MinecartElement> link, float rotation, int ticks) {
		return new AnimateMinecartInstruction(link, new Vec3(0, rotation, 0), ticks,
			(wse, v) -> wse.setRotation((float) v.y, ticks == 0), MinecartElement::getRotation);
	}

	public static AnimateMinecartInstruction move(ElementLink<MinecartElement> link, Vec3 offset, int ticks) {
		return new AnimateMinecartInstruction(link, offset, ticks, (wse, v) -> wse.setPositionOffset(v, ticks == 0),
			MinecartElement::getPositionOffset);
	}

	protected AnimateMinecartInstruction(ElementLink<MinecartElement> link, Vec3 totalDelta, int ticks,
		BiConsumer<MinecartElement, Vec3> setter, Function<MinecartElement, Vec3> getter) {
		super(link, totalDelta, ticks, setter, getter);
	}

}
