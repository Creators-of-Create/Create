package com.simibubi.create.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.lib.event.KeyInputCallback;
import com.simibubi.create.lib.event.MouseButtonCallback;
import com.simibubi.create.lib.event.MouseScrolledCallback;

import net.minecraft.client.Minecraft;
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

	public static InteractionResult onClickInput(int button, int action, int mods) {
		if (Minecraft.getInstance().screen != null)
			return InteractionResult.PASS;

		if (Minecraft.getInstance().options.keyPickItem.matchesMouse(button)) {
			if (ToolboxHandlerClient.onPickItem())
				return InteractionResult.SUCCESS;
			return InteractionResult.PASS;
		}

		if (button == 1)
			LinkedControllerClientHandler.deactivateInLectern();
		return InteractionResult.PASS;
	}

	public static void register() {
		KeyInputCallback.EVENT.register(InputEvents::onKeyInput);
		MouseScrolledCallback.EVENT.register(InputEvents::onMouseScrolled);
		MouseButtonCallback.EVENT.register(InputEvents::onMouseInput);
		MouseButtonCallback.EVENT.register(InputEvents::onClickInput);
	}

}
