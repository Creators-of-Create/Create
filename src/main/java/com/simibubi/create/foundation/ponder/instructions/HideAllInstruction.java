package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.elements.AnimatedOverlayElement;
import com.simibubi.create.foundation.ponder.elements.AnimatedSceneElement;

import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class HideAllInstruction extends TickingInstruction {

	private Direction fadeOutTo;

	public HideAllInstruction(int fadeOutTicks, Direction fadeOutTo) {
		super(false, fadeOutTicks);
		this.fadeOutTo = fadeOutTo;
	}

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		scene.getElements()
			.forEach(element -> {
				if (element instanceof AnimatedSceneElement) {
					AnimatedSceneElement animatedSceneElement = (AnimatedSceneElement) element;
					animatedSceneElement.setFade(1);
					animatedSceneElement
						.setFadeVec(fadeOutTo == null ? null : new Vec3d(fadeOutTo.getDirectionVec()).scale(.5f));
				} else if (element instanceof AnimatedOverlayElement) {
					AnimatedOverlayElement animatedSceneElement = (AnimatedOverlayElement) element;
					animatedSceneElement.setFade(1);
				} else
					element.setVisible(false);
			});
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		float fade = (remainingTicks / (float) totalTicks);

		scene.forEach(AnimatedSceneElement.class, ase -> {
			ase.setFade(fade * fade);
			if (remainingTicks == 0)
				ase.setFade(0);
		});
		
		scene.forEach(AnimatedOverlayElement.class, aoe -> {
			aoe.setFade(fade * fade);
			if (remainingTicks == 0)
				aoe.setFade(0);
		});
	}

}
