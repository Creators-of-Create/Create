package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.client.accessor.FontAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class FontRenderUtil {

	public static FontSet getFontStorage(Font renderer, ResourceLocation location) {
		return get(renderer).create$getFontSet(location);
	}

	private static FontAccessor get(Font renderer) {
		return MixinHelper.cast(renderer);
	}

	private FontRenderUtil() {}
}
