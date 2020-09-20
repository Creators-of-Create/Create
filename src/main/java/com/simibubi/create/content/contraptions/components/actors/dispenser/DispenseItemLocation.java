package com.simibubi.create.content.contraptions.components.actors.dispenser;

public class DispenseItemLocation {
	private final boolean internal;
	private final int slot;

	public static final DispenseItemLocation NONE = new DispenseItemLocation(false, -1);

	public DispenseItemLocation(boolean internal, int slot) {
		this.internal = internal;
		this.slot = slot;
	}

	public boolean isInternal() {
		return internal;
	}

	public int getSlot() {
		return slot;
	}

	public boolean isEmpty() {
		return slot < 0;
	}
}
