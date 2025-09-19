package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.SubtitleOverlay;

@Mixin(Gui.class)
public interface GuiAccessor {
	@Accessor("subtitleOverlay")
	SubtitleOverlay create$getSubtitleOverlay();
}
