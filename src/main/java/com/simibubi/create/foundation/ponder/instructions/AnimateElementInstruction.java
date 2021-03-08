package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.PonderSceneElement;

import net.minecraft.util.math.Vec3d;

public class AnimateElementInstruction<T extends PonderSceneElement> extends TickingInstruction {

	protected Vec3d deltaPerTick;
	protected Vec3d totalDelta;
	protected Vec3d target;
	protected ElementLink<T> link;
	protected T element;

	private BiConsumer<T, Vec3d> setter;
	private Function<T, Vec3d> getter;

	protected AnimateElementInstruction(ElementLink<T> link, Vec3d totalDelta, int ticks,
		BiConsumer<T, Vec3d> setter, Function<T, Vec3d> getter) {
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
