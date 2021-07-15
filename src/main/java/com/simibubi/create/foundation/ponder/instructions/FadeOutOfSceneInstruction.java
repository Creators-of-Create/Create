package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.AnimatedSceneElement;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class FadeOutOfSceneInstruction<T extends AnimatedSceneElement> extends TickingInstruction {

	private Direction fadeOutTo;
	private ElementLink<T> link;
	private T element;

	public FadeOutOfSceneInstruction(int fadeOutTicks, Direction fadeOutTo, ElementLink<T> link) {
		super(false, fadeOutTicks);
		this.fadeOutTo = fadeOutTo.getOpposite();
		this.link = link;
	}

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		element = scene.resolve(link);
		if (element == null)
			return;
		element.setVisible(true);
		element.setFade(1);
		element.setFadeVec(Vector3d.atLowerCornerOf(fadeOutTo.getNormal()).scale(.5f));
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (element == null)
			return;
		float fade = (remainingTicks / (float) totalTicks);
		element.setFade(1 - (1 - fade) * (1 - fade));
		if (remainingTicks == 0) {
			element.setVisible(false);
			element.setFade(0);
		}
	}

}
