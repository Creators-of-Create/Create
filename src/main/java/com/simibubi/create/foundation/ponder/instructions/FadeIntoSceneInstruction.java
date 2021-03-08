package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.AnimatedSceneElement;

import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class FadeIntoSceneInstruction<T extends AnimatedSceneElement> extends TickingInstruction {

	private Direction fadeInFrom;
	protected T element;
	private ElementLink<T> elementLink;

	public FadeIntoSceneInstruction(int fadeInTicks, Direction fadeInFrom, T element) {
		super(false, fadeInTicks);
		this.fadeInFrom = fadeInFrom;
		this.element = element;
	}

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		scene.addElement(element);
		element.setFade(0);
		element.setFadeVec(new Vec3d(fadeInFrom.getDirectionVec()).scale(.5f));
		if (elementLink != null)
			scene.linkElement(element, elementLink);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		float fade = (remainingTicks / (float) totalTicks);
		element.setFade(1 - fade * fade);
		if (remainingTicks == 0)
			element.setFade(1);
	}

	public ElementLink<T> createLink(PonderScene scene) {
		elementLink = new ElementLink<>(getElementClass());
		scene.linkElement(element, elementLink);
		return elementLink;
	}

	protected abstract Class<T> getElementClass();

}
