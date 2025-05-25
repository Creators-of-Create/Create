package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {
	@Accessor("subtitleOverlay")
	SubtitleOverlay create$getSubtitleOverlay();

	@Accessor("toolHighlightTimer")
	int create$getToolHighlightTimer();

	@Invoker("renderTextureOverlay")
	void create$renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha);
}
