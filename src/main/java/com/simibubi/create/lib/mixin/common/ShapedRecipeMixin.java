package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonArray;
import com.simibubi.create.Create;
import com.simibubi.create.lib.util.Constants;

import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
	@ModifyConstant(
			method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 0)
	)
	private static int create$modifyMaxHeight(int original) {
		return Constants.Crafting.HEIGHT;
	}

	@ModifyConstant(
			method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 1)
	)
	private static int create$modifyMaxWidth(int original) {
		return Constants.Crafting.WIDTH;
	}

	@Inject(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/gson/JsonSyntaxException;<init>(Ljava/lang/String;)V",
					shift = At.Shift.BEFORE,
					remap = false
			)
	)
	private static void create$changeWidthAndHeightWarning(JsonArray jsonArray, CallbackInfoReturnable<String[]> cir) {
		Create.LOGGER.warn("The following error may be inaccurate, there is no check for the actual maximum size of a recipe.");
	}
}
