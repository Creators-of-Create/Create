package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.elements.AnimatedSceneElement;

import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class FadeIntoSceneInstruction<T extends AnimatedSceneElement> extends TickingInstruction {

	private Direction fadeInFrom;
	private T element;

	public FadeIntoSceneInstruction(int fadeInTicks, Direction fadeInFrom, T element) {
			super(false, fadeInTicks);
			this.fadeInFrom = fadeInFrom;
			this.element = element;
		}

	@Override
	protected void firstTick(MetaDocScene scene) {
		super.firstTick(scene);
		scene.addElement(element);
		element.setFade(0);
		element.setFadeVec(new Vec3d(fadeInFrom.getDirectionVec()).scale(.5f));
	}

	@Override
	public void tick(MetaDocScene scene) {
		super.tick(scene);
		float fade = (remainingTicks / (float) totalTicks);
		element.setFade(1 - fade * fade);
		if (remainingTicks == 0)
			element.setFade(1);
	}

}
