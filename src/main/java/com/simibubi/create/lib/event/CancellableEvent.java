package com.simibubi.create.lib.event;

public class CancellableEvent {
	private boolean canceled;

	public void setCanceled(boolean cancelled) {
		this.canceled = cancelled;
	}

	public boolean isCanceled() {
		return canceled;
	}
}
