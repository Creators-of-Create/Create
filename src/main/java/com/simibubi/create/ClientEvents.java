package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.modules.contraptions.KineticDebugger;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.ChassisRangeDisplay;
import com.simibubi.create.modules.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.modules.contraptions.relays.belt.BeltConnectorItemHandler;
import com.simibubi.create.modules.curiosities.zapper.terrainzapper.TerrainZapperRenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
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
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

	private static final String itemPrefix = "item." + Create.ID;
	private static final String blockPrefix = "block." + Create.ID;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;

		AnimationTickHolder.tick();

		if (!isGameActive())
			return;

		if (!KineticDebugger.isActive() && KineticTileEntityRenderer.rainbowMode) {
			KineticTileEntityRenderer.rainbowMode = false;
			CreateClient.bufferCache.invalidate();
		}

		ScreenOpener.tick();
		onGameTick();
	}

	public static void onGameTick() {
		CreateClient.gameTick();
		BeltConnectorItemHandler.gameTick();
		TerrainZapperRenderHandler.tick();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		CreateClient.schematicHandler.render();
		CreateClient.schematicAndQuillHandler.render();
		CreateClient.schematicHologram.render();
		KineticDebugger.renderSourceOutline();
		ChassisRangeDisplay.renderOutlines(event.getPartialTicks());
		TerrainZapperRenderHandler.render();
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
				|| CreateClient.schematicAndQuillHandler.mouseScrolled(delta) 
				|| FilteringHandler.onScroll(delta) 
				|| ScrollValueHandler.onScroll(delta);
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
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;

		ItemStack stack = event.getItemStack();
		String translationKey = stack.getItem().getTranslationKey(stack);
		if (!translationKey.startsWith(itemPrefix) && !translationKey.startsWith(blockPrefix))
			return;

		if (TooltipHelper.hasTooltip(stack)) {
			List<ITextComponent> itemTooltip = event.getToolTip();
			List<ITextComponent> toolTip = new ArrayList<>();
			toolTip.add(itemTooltip.remove(0));
			TooltipHelper.getTooltip(stack).addInformation(toolTip);
			itemTooltip.addAll(0, toolTip);
		}

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
