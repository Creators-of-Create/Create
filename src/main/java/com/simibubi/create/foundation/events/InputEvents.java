package com.simibubi.create.foundation.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.elevator.ElevatorControlsHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class InputEvents {

	@SubscribeEvent
	public static void onKeyInput(KeyInputEvent event) {
		if (Minecraft.getInstance().screen != null)
			return;

		int key = event.getKey();
		boolean pressed = !(event.getAction() == 0);

		CreateClient.SCHEMATIC_HANDLER.onKeyInput(key, pressed);
		ToolboxHandlerClient.onKeyInput(key, pressed);
	}

	@SubscribeEvent
	public static void onMouseScrolled(MouseScrollEvent event) {
		if (Minecraft.getInstance().screen != null)
			return;

		double delta = event.getScrollDelta();
//		CollisionDebugger.onScroll(delta);
		boolean cancelled = CreateClient.SCHEMATIC_HANDLER.mouseScrolled(delta)
			|| CreateClient.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(delta) || TrainHUD.onScroll(delta)
			|| ElevatorControlsHandler.onScroll(delta);
		event.setCanceled(cancelled);
	}

	@SubscribeEvent
	public static void onMouseInput(MouseInputEvent event) {
		if (Minecraft.getInstance().screen != null)
			return;

		int button = event.getButton();
		boolean pressed = !(event.getAction() == 0);

		CreateClient.SCHEMATIC_HANDLER.onMouseInput(button, pressed);
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(button, pressed);
	}

	@SubscribeEvent
	public static void onClickInput(ClickInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null)
			return;

		if (CurvedTrackInteraction.onClickInput(event)) {
			event.setCanceled(true);
			return;
		}

		KeyMapping key = event.getKeyMapping();

		if (key == mc.options.keyUse || key == mc.options.keyAttack) {
			if (CreateClient.GLUE_HANDLER.onMouseInput(key == mc.options.keyAttack))
				event.setCanceled(true);
		}

		if (key == mc.options.keyPickItem) {
			if (ToolboxHandlerClient.onPickItem())
				event.setCanceled(true);
			return;
		}

		if (!event.isUseItem())
			return;

		LinkedControllerClientHandler.deactivateInLectern();
		TrainRelocator.onClicked(event);
	}

}
