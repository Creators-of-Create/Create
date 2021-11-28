package com.simibubi.create.foundation.ponder.instruction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.PonderSceneElement;

import net.minecraft.world.phys.Vec3;

public class AnimateElementInstruction<T extends PonderSceneElement> extends TickingInstruction {

	protected Vec3 deltaPerTick;
	protected Vec3 totalDelta;
	protected Vec3 target;
	protected ElementLink<T> link;
	protected T element;

	private BiConsumer<T, Vec3> setter;
	private Function<T, Vec3> getter;

	protected AnimateElementInstruction(ElementLink<T> link, Vec3 totalDelta, int ticks,
		BiConsumer<T, Vec3> setter, Function<T, Vec3> getter) {
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
			setter.accept(element, target);
			return;
		}
		setter.accept(element, getter.apply(element)
			.add(deltaPerTick));
	}

}
