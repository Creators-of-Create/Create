package com.simibubi.create.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorControlsHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.content.logistics.trains.entity.TrainRelocator;
import com.simibubi.create.content.logistics.trains.track.CurvedTrackInteraction;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class InputEvents {

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		if (Minecraft.getInstance().screen != null)
			return;

		int key = event.getKey();
		boolean pressed = !(event.getAction() == 0);

		CreateClient.SCHEMATIC_HANDLER.onKeyInput(key, pressed);
		ToolboxHandlerClient.onKeyInput(key, pressed);
	}

	@SubscribeEvent
	public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
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
	public static void onMouseInput(InputEvent.MouseButton.Pre event) {
		if (Minecraft.getInstance().screen != null)
			return;

		int button = event.getButton();
		boolean pressed = !(event.getAction() == 0);

		if (CreateClient.SCHEMATIC_HANDLER.onMouseInput(button, pressed))
			event.setCanceled(true);
		else if (CreateClient.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(button, pressed))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
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
