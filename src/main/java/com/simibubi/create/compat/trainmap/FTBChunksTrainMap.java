package com.simibubi.create.compat.trainmap;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class FTBChunksTrainMap {

	private static int cancelTooltips = 0;
	private static boolean renderingTooltip = false;
	private static boolean requesting;

	public static void tick() {
		if (cancelTooltips > 0)
			cancelTooltips--;
		if (!AllConfigs.client().showTrainMapOverlay.get()
			|| getAsLargeMapScreen(Minecraft.getInstance().screen) == null) {
			if (requesting)
				TrainMapSyncClient.stopRequesting();
			requesting = false;
			return;
		}
		TrainMapManager.tick();
		requesting = true;
		TrainMapSyncClient.requestData();
	}

	public static void cancelTooltips(RenderTooltipEvent.Pre event) {
		if (getAsLargeMapScreen(Minecraft.getInstance().screen) == null)
			return;
		if (renderingTooltip || cancelTooltips == 0)
			return;
		event.setCanceled(true);
	}

	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		LargeMapScreen screen = getAsLargeMapScreen(Minecraft.getInstance().screen);
		if (screen == null)
			return;
		if (TrainMapManager.handleToggleWidgetClick(screen.getMouseX(), screen.getMouseY(), 20, 2))
			event.setCanceled(true);
	}

	public static void renderGui(ScreenEvent.Render.Post event) {
		LargeMapScreen largeMapScreen = getAsLargeMapScreen(event.getScreen());
		if (largeMapScreen == null)
			return;
		Object panel = ObfuscationReflectionHelper.getPrivateValue(LargeMapScreen.class, largeMapScreen, "regionPanel");
		if (!(panel instanceof RegionMapPanel regionMapPanel))
			return;
		GuiGraphics graphics = event.getGuiGraphics();
		if (!AllConfigs.client().showTrainMapOverlay.get()) {
			renderToggleWidgetAndTooltip(event, largeMapScreen, graphics);
			return;
		}

		int blocksPerRegion = 16 * 32;
		int minX = Mth.floor(regionMapPanel.getScrollX());
		int minY = Mth.floor(regionMapPanel.getScrollY());
		float regionTileSize = largeMapScreen.getRegionTileSize() / (float) blocksPerRegion;
		int regionMinX =
			ObfuscationReflectionHelper.getPrivateValue(RegionMapPanel.class, regionMapPanel, "regionMinX");
		int regionMinZ =
			ObfuscationReflectionHelper.getPrivateValue(RegionMapPanel.class, regionMapPanel, "regionMinZ");
		float mouseX = event.getMouseX();
		float mouseY = event.getMouseY();

		boolean linearFiltering = largeMapScreen.getRegionTileSize() * Minecraft.getInstance()
			.getWindow()
			.getGuiScale() < 512D;

		PoseStack pose = graphics.pose();
		pose.pushPose();

		pose.translate(-minX, -minY, 0);
		pose.scale(regionTileSize, regionTileSize, 1);
		pose.translate(-regionMinX * blocksPerRegion, -regionMinZ * blocksPerRegion, 0);

		mouseX += minX;
		mouseY += minY;
		mouseX /= regionTileSize;
		mouseY /= regionTileSize;
		mouseX += regionMinX * blocksPerRegion;
		mouseY += regionMinZ * blocksPerRegion;

		Rect2i bounds = new Rect2i(Mth.floor(minX / regionTileSize + regionMinX * blocksPerRegion),
			Mth.floor(minY / regionTileSize + regionMinZ * blocksPerRegion),
			Mth.floor(largeMapScreen.width / regionTileSize), Mth.floor(largeMapScreen.height / regionTileSize));

		List<FormattedText> tooltip = TrainMapManager.renderAndPick(graphics, Mth.floor(mouseX), Mth.floor(mouseY),
			event.getPartialTick(), linearFiltering, bounds);

		pose.popPose();

		if (!renderToggleWidgetAndTooltip(event, largeMapScreen, graphics) && tooltip != null) {
			renderingTooltip = true;
			RemovedGuiUtils.drawHoveringText(graphics, tooltip, event.getMouseX(), event.getMouseY(),
				largeMapScreen.width, largeMapScreen.height, 256, Minecraft.getInstance().font);
			renderingTooltip = false;
			cancelTooltips = 5;
		}

		pose.pushPose();
		pose.translate(0, 0, 300);
		for (Widget widget : largeMapScreen.getWidgets()) {
			if (!widget.isEnabled())
				continue;
			if (widget == panel)
				continue;
			widget.draw(graphics, largeMapScreen.getTheme(), widget.getPosX(), widget.getPosY(), widget.getWidth(),
				widget.getHeight());
		}
		pose.popPose();
	}

	private static boolean renderToggleWidgetAndTooltip(ScreenEvent.Render.Post event, LargeMapScreen largeMapScreen,
		GuiGraphics graphics) {
		TrainMapManager.renderToggleWidget(graphics, 20, 2);
		if (!TrainMapManager.isToggleWidgetHovered(event.getMouseX(), event.getMouseY(), 20, 2))
			return false;

		renderingTooltip = true;
		RemovedGuiUtils.drawHoveringText(graphics, List.of(Lang.translate("train_map.toggle")
			.component()), event.getMouseX(), event.getMouseY() + 20, largeMapScreen.width, largeMapScreen.height, 256,
			Minecraft.getInstance().font);
		renderingTooltip = false;
		cancelTooltips = 5;
		return true;
	}

	private static LargeMapScreen getAsLargeMapScreen(Screen screen) {
		if (!(screen instanceof ScreenWrapper screenWrapper))
			return null;
		Object wrapped = ObfuscationReflectionHelper.getPrivateValue(ScreenWrapper.class, screenWrapper, "wrappedGui");
		if (!(wrapped instanceof LargeMapScreen largeMapScreen))
			return null;
		return largeMapScreen;
	}

}
