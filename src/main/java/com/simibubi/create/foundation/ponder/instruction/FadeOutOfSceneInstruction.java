package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.AnimatedSceneElement;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class FadeOutOfSceneInstruction<T extends AnimatedSceneElement> extends TickingInstruction {

	private Direction fadeOutTo;
	private ElementLink<T> link;
	private T element;

	public FadeOutOfSceneInstruction(int fadeOutTicks, Direction fadeOutTo, ElementLink<T> link) {
		super(false, fadeOutTicks);
		this.fadeOutTo = fadeOutTo == null ? null : fadeOutTo.getOpposite();
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
		element.setFadeVec(fadeOutTo == null ? Vec3.ZERO
			: Vec3.atLowerCornerOf(fadeOutTo.getNormal())
				.scale(.5f));
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
