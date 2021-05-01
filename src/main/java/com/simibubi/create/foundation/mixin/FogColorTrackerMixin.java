package com.simibubi.create.foundation.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.gl.GlFog;
import com.mojang.blaze3d.platform.GlStateManager;

@Mixin(GlStateManager.class)
public class FogColorTrackerMixin {

	@Inject(at = @At("TAIL"), method = "fog")
	private static void copyFogColor(int pname, float[] params, CallbackInfo ci) {
		if (pname == GL11.GL_FOG_COLOR) {
			GlFog.FOG_COLOR = params;
		}
	}
}
