package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

import net.minecraft.util.math.Vec3d;

public class AnimateWorldSectionInstruction extends TickingInstruction {

	protected Vec3d deltaPerTick;
	protected Vec3d totalDelta;
	protected Vec3d target;
	protected ElementLink<WorldSectionElement> link;
	protected WorldSectionElement element;

	private BiConsumer<WorldSectionElement, Vec3d> setter;
	private Function<WorldSectionElement, Vec3d> getter;

	public static AnimateWorldSectionInstruction rotate(ElementLink<WorldSectionElement> link, Vec3d rotation,
		int ticks) {
		return new AnimateWorldSectionInstruction(link, rotation, ticks, WorldSectionElement::setAnimatedRotation,
			WorldSectionElement::getAnimatedRotation);
	}

	public static AnimateWorldSectionInstruction move(ElementLink<WorldSectionElement> link, Vec3d offset, int ticks) {
		return new AnimateWorldSectionInstruction(link, offset, ticks, WorldSectionElement::setAnimatedOffset,
			WorldSectionElement::getAnimatedOffset);
	}

	protected AnimateWorldSectionInstruction(ElementLink<WorldSectionElement> link, Vec3d totalDelta, int ticks,
		BiConsumer<WorldSectionElement, Vec3d> setter, Function<WorldSectionElement, Vec3d> getter) {
		super(false, ticks);
		this.link = link;
		this.setter = setter;
		this.getter = getter;
		this.deltaPerTick = totalDelta.scale(1d / ticks);
		this.totalDelta = totalDelta;
		this.target = totalDelta;
	}

	@Override
	protected final void firstTick(PonderScene scene) {
		super.firstTick(scene);
		element = scene.resolve(link);
		if (element == null)
			return;
		target = getter.apply(element)
			.add(totalDelta);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (element == null)
			return;
		if (remainingTicks == 0) {
			setter.accept(element, target);
			return;
		}
		setter.accept(element, getter.apply(element)
			.add(deltaPerTick));
	}

}
