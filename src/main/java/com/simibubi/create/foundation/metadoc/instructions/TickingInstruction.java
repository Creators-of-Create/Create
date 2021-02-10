package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocInstruction;
import com.simibubi.create.foundation.metadoc.MetaDocScene;

public abstract class TickingInstruction extends MetaDocInstruction {

	private boolean blocking;
	protected int totalTicks;
	protected int remainingTicks;

	public TickingInstruction(boolean blocking, int ticks) {
		this.blocking = blocking;
		remainingTicks = totalTicks = ticks;
	}

	@Override
	public void reset(MetaDocScene scene) {
		super.reset(scene);
		remainingTicks = totalTicks;
	}
	
	protected void firstTick(MetaDocScene scene) {}

	@Override
	public void tick(MetaDocScene scene) {
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
