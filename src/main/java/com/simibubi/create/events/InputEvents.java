package com.simibubi.create.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {

	@SubscribeEvent
	public static void onKeyInput(KeyInputEvent event) {
		int key = event.getKey();
		boolean pressed = !(event.getAction() == 0);

		if (Minecraft.getInstance().currentScreen != null)
			return;

		CreateClient.SCHEMATIC_HANDLER.onKeyInput(key, pressed);
	}

	@SubscribeEvent
	public static void onMouseScrolled(MouseScrollEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;

		double delta = event.getScrollDelta();
//		CollisionDebugger.onScroll(delta);
		boolean cancelled = CreateClient.SCHEMATIC_HANDLER.mouseScrolled(delta)
				|| CreateClient.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(delta) || FilteringHandler.onScroll(delta)
				|| ScrollValueHandler.onScroll(delta);
		event.setCanceled(cancelled);
	}

	@SubscribeEvent
	public static void onMouseInput(MouseInputEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;

		int button = event.getButton();
		boolean pressed = !(event.getAction() == 0);

		CreateClient.SCHEMATIC_HANDLER.onMouseInput(button, pressed);
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(button, pressed);
	}

}
