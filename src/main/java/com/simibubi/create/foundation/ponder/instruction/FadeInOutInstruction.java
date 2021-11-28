package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.PonderScene;

public abstract class FadeInOutInstruction extends TickingInstruction {

	protected static final int fadeTime = 5;

	public FadeInOutInstruction(int duration) {
		super(false, duration + 2 * fadeTime);
	}

	protected abstract void show(PonderScene scene);

	protected abstract void hide(PonderScene scene);

	protected abstract void applyFade(PonderScene scene, float fade);

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		show(scene);
		applyFade(scene, 0);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int elapsed = totalTicks - remainingTicks;

		if (elapsed < fadeTime) {
			float fade = (elapsed / (float) fadeTime);
			applyFade(scene, fade * fade);

		} else if (remainingTicks < fadeTime) {
			float fade = (remainingTicks / (float) fadeTime);
			applyFade(scene, fade * fade);

		} else
			applyFade(scene, 1);

		if (remainingTicks == 0) {
			applyFade(scene, 0);
			hide(scene);
		}

	}

}
