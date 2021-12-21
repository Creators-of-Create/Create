package com.simibubi.create.lib.util;

import java.util.Locale;

import com.simibubi.create.lib.extensions.LanguageInfoExtensions;
import com.simibubi.create.lib.mixin.accessor.MinecraftAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class MinecraftClientUtil {
	public static float getRenderPartialTicksPaused(Minecraft minecraft) {
		return get(minecraft).create$pausePartialTick();
	}

	public static Locale getLocale() {
		return ((LanguageInfoExtensions) Minecraft.getInstance().getLanguageManager().getSelected()).create$getJavaLocale();
	}

	private static MinecraftAccessor get(Minecraft minecraft) {
		return MixinHelper.cast(minecraft);
	}

	private MinecraftClientUtil() {}
}
