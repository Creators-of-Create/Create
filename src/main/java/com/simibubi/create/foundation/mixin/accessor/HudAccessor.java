package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.SubtitleOverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {
	@Accessor("subtitleOverlay")
	SubtitleOverlay create$getSubtitleOverlay();

	@Accessor("toolHighlightTimer")
	int create$getToolHighlightTimer();
}
