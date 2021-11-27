package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.PonderScene;

public abstract class TickingInstruction extends PonderInstruction {

	private boolean blocking;
	protected int totalTicks;
	protected int remainingTicks;

	public TickingInstruction(boolean blocking, int ticks) {
		this.blocking = blocking;
		remainingTicks = totalTicks = ticks;
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		remainingTicks = totalTicks;
	}
	
	protected void firstTick(PonderScene scene) {}
	
	@Override
	public void onScheduled(PonderScene scene) {
		super.onScheduled(scene);
		if (isBlocking())
			scene.addToSceneTime(totalTicks);
	}

	@Override
	public void tick(PonderScene scene) {
		if (remainingTicks == totalTicks)
			firstTick(scene);
		if (remainingTicks > 0)
			remainingTicks--;
	}

	@Override
	public boolean isComplete() {
		return remainingTicks == 0;
	}

	@Override
	public boolean isBlocking() {
		return blocking;
	}

}
