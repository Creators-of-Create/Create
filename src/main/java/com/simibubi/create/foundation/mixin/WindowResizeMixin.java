package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.gui.UIRenderHelper;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class WindowResizeMixin {

	@Shadow @Final private Window window;

	@Inject(at = @At("TAIL"), method = "resizeDisplay")
	private void updateWindowSize(CallbackInfo ci) {
		UIRenderHelper.updateWindowSize(window);
	}

}
