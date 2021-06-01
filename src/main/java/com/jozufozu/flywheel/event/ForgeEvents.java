package com.jozufozu.flywheel.event;

import java.util.ArrayList;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeEvents {

	@SubscribeEvent
	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {

		if (Minecraft.getInstance().gameSettings.showDebugInfo) {

			ArrayList<String> right = event.getRight();

			String text = "Flywheel: " + Backend.getBackendDescriptor();
			if (right.size() < 10) {
				right.add("");
				right.add(text);
			} else {
				right.add(9, "");
				right.add(10, text);
			}
		}
	}
}
