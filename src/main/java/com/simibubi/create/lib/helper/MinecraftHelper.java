package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.MinecraftAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class MinecraftHelper {
	public static float getRenderPartialTicksPaused(Minecraft minecraft) {
		return get(minecraft).create$pausePartialTick();
	}

	private static MinecraftAccessor get(Minecraft minecraft) {
		return MixinHelper.cast(minecraft);
	}

	private MinecraftHelper() {}
}
