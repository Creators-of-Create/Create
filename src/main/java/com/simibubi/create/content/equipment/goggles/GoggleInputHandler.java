package com.simibubi.create.content.equipment.goggles;

import com.simibubi.create.AllKeys;
import net.minecraft.client.Minecraft;

public class GoggleInputHandler {

	public static void onKeyInput(int key, boolean pressed) {
		if (!pressed)
			return;

		if (AllKeys.TOGGLE_GOGGLES.doesModifierAndCodeMatch(key)) {
			GoggleOverlayRenderer.toggleGoggleOverlay();
			Minecraft.getInstance().player.displayClientMessage(
				net.minecraft.network.chat.Component.literal("Goggle overlay " + 
					(GoggleOverlayRenderer.goggleOverlayEnabled ? "enabled" : "disabled")), 
				true);
		}
	}

}
