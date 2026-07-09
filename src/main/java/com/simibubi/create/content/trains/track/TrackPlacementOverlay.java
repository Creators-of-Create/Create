package com.simibubi.create.content.trains.track;

import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.foundation.mixin.accessor.HudAccessor;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;

public class TrackPlacementOverlay implements GuiLayer {
	public static final TrackPlacementOverlay INSTANCE = new TrackPlacementOverlay();

	@Override
	public void render(GuiGraphicsExtractor GuiGraphicsExtractor, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		if (TrackPlacement.hoveringPos == null)
			return;
		if (TrackPlacement.cached == null || TrackPlacement.cached.curve == null || !TrackPlacement.cached.valid)
			return;
		if (TrackPlacement.extraTipWarmup < 4)
			return;

		if (((HudAccessor) mc.gui.hud).create$getToolHighlightTimer() > 0)
			return;

		boolean active = mc.options.keySprint.isDown();
        MutableComponent text = CreateLang.translateDirect("track.hold_for_smooth_curve", Component.keybind("key.sprint")
			.withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY));

		Window window = mc.getWindow();
		int x = (window.getGuiScaledWidth() - mc.font.width(text)) / 2;
		int y = window.getGuiScaledHeight() - 61;
		Color color = new Color(0x4ADB4A).setAlpha(Mth.clamp((TrackPlacement.extraTipWarmup - 4) / 3f, 0.1f, 1));
		GuiGraphicsExtractor.text(mc.font, text, x, y, color.getRGB(), false);

	}

}
