package com.simibubi.create.compat.trainmap;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;

import net.minecraft.world.level.Level;

import net.minecraftforge.client.event.InputEvent;

import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.gui.ScreenBase;

import java.util.List;

public class XaeroTrainMap {

	private static boolean requesting;
	private static ResourceKey<Level> renderedDimension;

	public static void tick() {
		if (!AllConfigs.client().showTrainMapOverlay.get() || !isMapOpen()) {
			if (requesting)
				TrainMapSyncClient.stopRequesting();
			requesting = false;
			return;
		}
		TrainMapManager.tick();
		requesting = true;
		TrainMapSyncClient.requestData();
	}

	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.screen instanceof GuiMap))
			return;

		Window window = mc.getWindow();
		double mX = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
		double mY = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

		if (TrainMapManager.handleToggleWidgetClick(Mth.floor(mX), Mth.floor(mY), 3, 30))
			event.setCanceled(true);
	}

	// Called by XaeroFullscreenMapMixin
	public static void onRender(GuiGraphics graphics, GuiMap screen, MapProcessor processor, double x, double z, int mX, int mY, double mapScale, float pt) {
		if (!AllConfigs.client().showTrainMapOverlay.get()) {
			renderToggleWidgetAndTooltip(graphics, screen, mX, mY);
			return;
		}

		renderedDimension = processor.getMapWorld().getCurrentDimension().getDimId();

		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();

		double guiScale = (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double scale = mapScale / guiScale;

		PoseStack pose = graphics.pose();
		pose.pushPose();

		pose.translate(screen.width / 2.0f, screen.height / 2.0f, 0);
		pose.scale((float) scale, (float) scale, 1);
		pose.translate(-x, -z, 0);

		float mouseX = mX - screen.width / 2.0f;
		float mouseY = mY - screen.height / 2.0f;
		mouseX /= scale;
		mouseY /= scale;
		mouseX += x;
		mouseY += z;

		Rect2i bounds =
			new Rect2i(Mth.floor(-screen.width / 2.0f / scale + x), Mth.floor(-screen.height / 2.0f / scale + z),
				Mth.floor(screen.width / scale), Mth.floor(screen.height / scale));

		List<FormattedText> tooltip =
			TrainMapManager.renderAndPick(graphics, Mth.floor(mouseX), Mth.floor(mouseY), pt, false, bounds);

		pose.popPose();

		if (!renderToggleWidgetAndTooltip(graphics, screen, mX, mY) && tooltip != null)
			RemovedGuiUtils.drawHoveringText(graphics, tooltip, mX, mY, screen.width, screen.height, 256, mc.font);
	}

	private static boolean renderToggleWidgetAndTooltip(GuiGraphics graphics, GuiMap screen, int mouseX,
														int mouseY) {
		TrainMapManager.renderToggleWidget(graphics, 3, 30);
		if (!TrainMapManager.isToggleWidgetHovered(mouseX, mouseY, 3, 30))
			return false;

		RemovedGuiUtils.drawHoveringText(graphics, List.of(CreateLang.translate("train_map.toggle")
			.component()), mouseX, mouseY + 20, screen.width, screen.height, 256, Minecraft.getInstance().font);
		return true;
	}

	public static ResourceKey<Level> getRenderedDimension(){
		return renderedDimension;
	}

	public static boolean isMapOpen(){
		return (Minecraft.getInstance().screen instanceof ScreenBase);
	}
}
