package com.simibubi.create.foundation.ponder;

public abstract class PonderInstruction {

	public boolean isBlocking() {
		return false;
	}

	public void reset(PonderScene scene) {}

	public abstract boolean isComplete();

	public abstract void tick(PonderScene scene);

}
