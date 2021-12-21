package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.lib.util.ScreenHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;

@Environment(EnvType.CLIENT)
@Mixin(GuiComponent.class)
public class GuiComponentMixin {
	@ModifyVariable(
			method = "fillGradient(Lcom/mojang/math/Matrix4f;Lcom/mojang/blaze3d/vertex/BufferBuilder;IIIIIII)V",
			at = @At("HEAD"),
			ordinal = 5,
			argsOnly = true
	)
	private static int create$replaceN(int n) {
		return getColor(n);
	}

	@ModifyVariable(
			method = "fillGradient(Lcom/mojang/math/Matrix4f;Lcom/mojang/blaze3d/vertex/BufferBuilder;IIIIIII)V",
			at = @At("HEAD"),
			ordinal = 6,
			argsOnly = true
	)
	private static int create$replaceO(int o) {
		return getColor(o);
	}

	private static int getColor(int original) {
		if (ScreenHelper.CURRENT_COLOR != null) {
			if (original == ScreenHelper.DEFAULT_BORDER_COLOR_START) {
				return ScreenHelper.CURRENT_COLOR.getBorderColorStart();
			} else if (original == ScreenHelper.DEFAULT_BORDER_COLOR_END) {
				return ScreenHelper.CURRENT_COLOR.getBorderColorEnd();
			}
		}
		return original;
	}
}
