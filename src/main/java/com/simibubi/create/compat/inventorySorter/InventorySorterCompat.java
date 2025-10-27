package com.simibubi.create.compat.inventorySorter;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

/**
 * Compatibility with cpw's InventorySorter.
 * We need to stop it from interfering with scrolling in the Redstone Requester's screen.
 */
public class InventorySorterCompat {
	public static final String SLOT_BLACKLIST = "slotblacklist";

	public static void init(IEventBus bus) {
		bus.addListener(InventorySorterCompat::sendImc);
	}

	private static void sendImc(InterModEnqueueEvent event) {
		InterModComms.sendTo(Mods.INVENTORYSORTER.id(), SLOT_BLACKLIST, SorterProofSlot.class::getName);
	}
}
