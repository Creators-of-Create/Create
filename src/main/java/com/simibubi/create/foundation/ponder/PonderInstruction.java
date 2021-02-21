package com.simibubi.create.foundation.ponder;

import java.util.function.Consumer;

public abstract class PonderInstruction {

	public boolean isBlocking() {
		return false;
	}

	public void reset(PonderScene scene) {}

	public abstract boolean isComplete();

	public abstract void tick(PonderScene scene);

	public static PonderInstruction simple(Consumer<PonderScene> callback) {
		return new Simple(callback);
	}
	
	private static class Simple extends PonderInstruction {

		private Consumer<PonderScene> callback;

		public Simple(Consumer<PonderScene> callback) {
			this.callback = callback;
		}
		
		@Override
		public boolean isComplete() {
			return true;
		}

		@Override
		public void tick(PonderScene scene) {
			callback.accept(scene);
		}
		
	}
	
}
