package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.FontAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class FontRendererHelper {

	public static FontSet getFontStorage(Font renderer, ResourceLocation location) {
		return get(renderer).create$getFontSet(location);
	}

	private static FontAccessor get(Font renderer) {
		return MixinHelper.cast(renderer);
	}

	private FontRendererHelper() {}
}
