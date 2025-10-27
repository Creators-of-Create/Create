package com.simibubi.create.content.trains.station;

import com.simibubi.create.Create;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class GlobalPackagePort {
	public String address = "";
	public ItemStackHandler offlineBuffer = new ItemStackHandler(18);
	public boolean primed = false;
	private boolean restoring = false;

	public void restoreOfflineBuffer(IItemHandlerModifiable inventory) {
		if (!primed) return;

		restoring = true;

		for (int slot = 0; slot < offlineBuffer.getSlots(); slot++) {
			inventory.setStackInSlot(slot, offlineBuffer.getStackInSlot(slot));
		}

		restoring = false;
		primed = false;
	}

	public void saveOfflineBuffer(IItemHandlerModifiable inventory) {
		/*
		 * Each time restoreOfflineBuffer changes a slot, the inventory
		 * calls this method. We must filter out those calls to prevent
		 * overwriting later slots which haven't been restored yet and
		 * to avoid unnecessary work.
		 */
		if (restoring) return;

		// TODO: Call save method on individual slots rather than iterating
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			offlineBuffer.setStackInSlot(slot, inventory.getStackInSlot(slot));
		}

		Create.RAILWAYS.markTracksDirty();
	}
}
