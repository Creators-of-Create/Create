package com.simibubi.create.infrastructure.command;

public class ServerLagger {

	private int tickTime;
	private boolean isLagging = false;

	public void tick() {
		if (!isLagging || tickTime <= 0)
			return;

		try {
			Thread.sleep(tickTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setTickTime(int tickTime) {
		this.tickTime = Math.max(tickTime, 0);
	}

	public void setLagging(boolean lagging) {
		this.isLagging = lagging;
	}

	public int getTickTime() {
		return tickTime;
	}

	public boolean isLagging() {
		return isLagging;
	}
}
