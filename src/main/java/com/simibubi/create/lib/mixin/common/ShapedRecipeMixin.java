package com.simibubi.create.lib.mixin.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonArray;
import com.simibubi.create.lib.extensions.ShapedRecipeExtensions;
import com.simibubi.create.lib.utility.Constants;

import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin implements ShapedRecipeExtensions {
	@Unique
	private static final Logger CREATE$LOGGER = LogManager.getLogger("Create");

	@ModifyConstant(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 0))
	private static int modifyMaxHeight(int original) {
		return Constants.Crafting.HEIGHT;
	}

	@ModifyConstant(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 1))
	private static int modifyMaxWidth(int original) {
		return Constants.Crafting.WIDTH;
	}

	// these should inject into before each thrown JsonSyntaxException
	// 🍝

	@Inject(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/google/gson/JsonSyntaxException;<init>(Ljava/lang/String;)V", remap = false))
	private static void changeWidthAndHeightWarning(JsonArray jsonArray, CallbackInfoReturnable<String[]> cir) {
		CREATE$LOGGER.warn("The following error may be inaccurate, there is no check for the actual maximum size of a recipe.");
	}

	@Override
	public void setCraftingSize(int width, int height) {
		if (Constants.Crafting.WIDTH < width) Constants.Crafting.WIDTH = width;
		if (Constants.Crafting.HEIGHT < height) Constants.Crafting.HEIGHT = height;
	}
}
