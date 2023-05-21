package com.simibubi.create.content.trains.track;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class TrackPlacementOverlay implements IGuiOverlay {
	public static final TrackPlacementOverlay INSTANCE = new TrackPlacementOverlay();
	
	@Override
	public void render(ForgeGui gui, PoseStack poseStack, float partialTicks, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		if (TrackPlacement.hoveringPos == null)
			return;
		if (TrackPlacement.cached == null || TrackPlacement.cached.curve == null || !TrackPlacement.cached.valid)
			return;
		if (TrackPlacement.extraTipWarmup < 4)
			return;
	
		if (ObfuscationReflectionHelper.getPrivateValue(Gui.class, gui,
			"toolHighlightTimer") instanceof Integer toolHighlightTimer && toolHighlightTimer > 0)
			return;
	
		boolean active = mc.options.keySprint.isDown();
		MutableComponent text = Lang.translateDirect("track.hold_for_smooth_curve", Components.keybind("key.sprint")
			.withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY));
	
		Window window = mc.getWindow();
		int x = (window.getGuiScaledWidth() - gui.getFont()
			.width(text)) / 2;
		int y = window.getGuiScaledHeight() - 61;
		Color color = new Color(0x4ADB4A).setAlpha(Mth.clamp((TrackPlacement.extraTipWarmup - 4) / 3f, 0.1f, 1));
		gui.getFont()
			.draw(poseStack, text, x, y, color.getRGB());
	
	}

}
