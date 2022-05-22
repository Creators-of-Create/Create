package com.simibubi.create.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import io.github.fabricators_of_create.porting_lib.event.client.KeyInputCallback;
import io.github.fabricators_of_create.porting_lib.event.client.MouseButtonCallback;
import io.github.fabricators_of_create.porting_lib.event.client.MouseScrolledCallback;

import io.github.fabricators_of_create.porting_lib.event.client.OnStartUseItemCallback;
import io.github.fabricators_of_create.porting_lib.event.client.PickBlockCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class InputEvents {

	public static void onKeyInput(int key, int scancode, int action, int mods) {
		if (Minecraft.getInstance().screen != null)
			return;

		boolean pressed = !(action == 0);

		CreateClient.SCHEMATIC_HANDLER.onKeyInput(key, pressed);
		ToolboxHandlerClient.onKeyInput(key, pressed);
	}

	public static boolean onMouseScrolled(double delta) {
		if (Minecraft.getInstance().screen != null)
			return false;

//		CollisionDebugger.onScroll(delta);
		boolean cancelled = CreateClient.SCHEMATIC_HANDLER.mouseScrolled(delta)
				|| CreateClient.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(delta) || FilteringHandler.onScroll(delta)
				|| ScrollValueHandler.onScroll(delta);
		return cancelled;
	}

	public static InteractionResult onMouseInput(int button, int action, int mods) {
		if (Minecraft.getInstance().screen != null)
			return InteractionResult.PASS;

		boolean pressed = !(action == 0);

		CreateClient.SCHEMATIC_HANDLER.onMouseInput(button, pressed);
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(button, pressed);
		return InteractionResult.PASS;
	}

	public static boolean onPickBlock() {
		if (Minecraft.getInstance().screen != null)
			return false;
		return ToolboxHandlerClient.onPickItem();
	}

	public static InteractionResult onStartUseItem(InteractionHand hand) {
		if (Minecraft.getInstance().screen == null)
			LinkedControllerClientHandler.deactivateInLectern();
		return InteractionResult.PASS;
	}

	public static void register() {
		KeyInputCallback.EVENT.register(InputEvents::onKeyInput);
		MouseScrolledCallback.EVENT.register(InputEvents::onMouseScrolled);
		MouseButtonCallback.EVENT.register(InputEvents::onMouseInput);
		OnStartUseItemCallback.EVENT.register(InputEvents::onStartUseItem);
		PickBlockCallback.EVENT.register(InputEvents::onPickBlock);
	}

}
