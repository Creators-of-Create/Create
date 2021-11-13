package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public interface TitleScreenAccessor {
	@Accessor("panorama")
	PanoramaRenderer getPanorama();
}
