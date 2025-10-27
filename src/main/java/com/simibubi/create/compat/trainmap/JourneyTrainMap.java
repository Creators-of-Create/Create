package com.simibubi.create.compat.trainmap;

import java.util.List;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.display.Context.UI;
import journeymap.api.v2.client.event.FullscreenRenderEvent;
import journeymap.api.v2.client.fullscreen.IFullscreen;
import journeymap.api.v2.client.util.UIState;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneyTrainMap implements IClientPlugin {

	private static boolean requesting;

	public JourneyTrainMap() {
	}

	@Override
	public void initialize(IClientAPI jmClientApi) {
		FullscreenEventRegistry.FULLSCREEN_RENDER_EVENT.subscribe(Create.ID, JourneyTrainMap::onRender);
	}

	@Override
	public String getModId() {
		return Create.ID;
	}

	public static void tick() {
		if (!AllConfigs.client().showTrainMapOverlay.get() || !(Minecraft.getInstance().screen instanceof Fullscreen)) {
			if (requesting)
				TrainMapSyncClient.stopRequesting();
			requesting = false;
			return;
		}
		TrainMapManager.tick();
		requesting = true;
		TrainMapSyncClient.requestData();
	}

	public static void mouseClick(Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.screen instanceof Fullscreen screen))
			return;

		Window window = mc.getWindow();
		double mX = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
		double mY = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

		if (TrainMapManager.handleToggleWidgetClick(Mth.floor(mX), Mth.floor(mY), 3, 30))
			event.setCanceled(true);
	}

	// GuiGraphics graphics, Fullscreen screen, double x, double z, int mX, int mY, float pt
	public static void onRender(FullscreenRenderEvent event) {
		GuiGraphics graphics = event.getGraphics();
		IFullscreen fullscreen = event.getFullscreen();
		Screen screen = fullscreen.getScreen();
		double x = fullscreen.getCenterBlockX(true);
		double z = fullscreen.getCenterBlockZ(true);
		int mX = event.getMouseX();
		int mY = event.getMouseY();
		float pt = event.getPartialTicks();

		UIState state = fullscreen.getUiState();
		if (state == null)
			return;
		if (state.ui != UI.Fullscreen)
			return;
		if (!state.active)
			return;
		if (!AllConfigs.client().showTrainMapOverlay.get()) {
			renderToggleWidgetAndTooltip(graphics, screen, mX, mY);
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();

		double guiScale = (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double scale = state.blockSize / guiScale;

		PoseStack pose = graphics.pose();
		pose.pushPose();

		pose.translate(screen.width / 2.0f, screen.height / 2.0f, 0);
		pose.scale((float) scale, (float) scale, 1);
		pose.translate(-x, -z, 0);

		float mouseX = mX - screen.width / 2.0f;
		float mouseY = mY - screen.height / 2.0f;
		mouseX /= (float) scale;
		mouseY /= (float) scale;

		Rect2i bounds =
			new Rect2i(Mth.floor(-screen.width / 2.0f / scale + x), Mth.floor(-screen.height / 2.0f / scale + z),
				Mth.floor(screen.width / scale), Mth.floor(screen.height / scale));

		List<FormattedText> tooltip =
			TrainMapManager.renderAndPick(graphics, Mth.floor(mouseX), Mth.floor(mouseY), false, bounds);

		pose.popPose();

		if (!renderToggleWidgetAndTooltip(graphics, screen, mX, mY) && tooltip != null)
			RemovedGuiUtils.drawHoveringText(graphics, tooltip, mX, mY, screen.width, screen.height, 256, mc.font);
	}

	private static boolean renderToggleWidgetAndTooltip(GuiGraphics graphics, Screen screen, int mouseX,
		int mouseY) {
		TrainMapManager.renderToggleWidget(graphics, 3, 30);
		if (!TrainMapManager.isToggleWidgetHovered(mouseX, mouseY, 3, 30))
			return false;

		RemovedGuiUtils.drawHoveringText(graphics, List.of(CreateLang.translate("train_map.toggle")
			.component()), mouseX, mouseY + 20, screen.width, screen.height, 256, Minecraft.getInstance().font);
		return true;
	}

}
