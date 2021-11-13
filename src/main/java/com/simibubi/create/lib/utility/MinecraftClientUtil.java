package com.simibubi.create.lib.utility;

import java.util.Locale;

import com.simibubi.create.lib.extensions.LanguageInfoExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class MinecraftClientUtil {
	public static Locale getLocale() {
		return ((LanguageInfoExtensions) Minecraft.getInstance().getLanguageManager().getSelected()).create$getJavaLocale();
	}
}
