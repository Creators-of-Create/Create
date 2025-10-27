package com.simibubi.create.compat.ftb;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class FTBIntegration {

	// Disabled until newer ftb library with their new config system has settled a bit

	private static boolean buttonStatePreviously;

	public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
//		forgeEventBus.addListener(EventPriority.HIGH, FTBIntegration::removeGUIClutterOpen);
//		forgeEventBus.addListener(EventPriority.LOW, FTBIntegration::removeGUIClutterClose);
	}

	private static void removeGUIClutterOpen(ScreenEvent.Opening event) {
		if (isCreate(event.getCurrentScreen()))
			return;
		if (!isCreate(event.getNewScreen()))
			return;
//		buttonStatePreviously = FTBLibraryClientConfig.SIDEBAR_ENABLED.get();
//		FTBLibraryClientConfig.SIDEBAR_ENABLED.set(false);
	}

	private static void removeGUIClutterClose(ScreenEvent.Closing event) {
		if (!isCreate(event.getScreen()))
			return;
//		FTBLibraryClientConfig.SIDEBAR_ENABLED.set(buttonStatePreviously);
	}

	private static boolean isCreate(Screen screen) {
		return screen instanceof AbstractSimiContainerScreen<?> || screen instanceof AbstractSimiScreen;
	}

}
