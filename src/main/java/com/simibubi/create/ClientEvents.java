package com.simibubi.create;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.modules.contraptions.receivers.TurntableHandler;
import com.simibubi.create.modules.contraptions.relays.belt.BeltItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (!isGameActive())
			return;

		ScreenOpener.tick();
		onGameTick();
	}

	public static void onGameTick() {
		CreateClient.gameTick();
		BeltItemHandler.gameTick();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		CreateClient.schematicHandler.render();
		CreateClient.schematicAndQuillHandler.render();
		CreateClient.schematicHologram.render();
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		onRenderHotbar();
	}

	public static void onRenderHotbar() {
		CreateClient.schematicHandler.renderOverlay();
	}

	@SubscribeEvent
	public static void onKeyInput(KeyInputEvent event) {
		int key = event.getKey();
		boolean pressed = !(event.getAction() == 0);

		if (Minecraft.getInstance().currentScreen != null)
			return;

		CreateClient.schematicHandler.onKeyInput(key, pressed);
	}

	
	@SubscribeEvent
	public static void onMouseScrolled(MouseScrollEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		
		double delta = event.getScrollDelta();

		boolean cancelled = CreateClient.schematicHandler.mouseScrolled(delta)
				|| CreateClient.schematicAndQuillHandler.mouseScrolled(delta);
		event.setCanceled(cancelled);
	}

	@SubscribeEvent
	public static void onMouseInput(MouseInputEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;

		int button = event.getButton();
		boolean pressed = !(event.getAction() == 0);

		CreateClient.schematicHandler.onMouseInput(button, pressed);
		CreateClient.schematicAndQuillHandler.onMouseInput(button, pressed);
	}

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		if (!isGameActive())
			return;
		
		TurntableHandler.gameRenderTick();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().world == null || Minecraft.getInstance().player == null);
	}

}
