package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.item.crafting.ShapedRecipe;

import static com.simibubi.create.lib.util.ShapedRecipeUtil.HEIGHT;
import static com.simibubi.create.lib.util.ShapedRecipeUtil.WIDTH;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
	@ModifyConstant(
			method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 0)
	)
	private static int create$modifyMaxHeight(int original) {
		return HEIGHT;
	}

	@ModifyConstant(
			method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(intValue = 3, ordinal = 1)
	)
	private static int create$modifyMaxWidth(int original) {
		return WIDTH;
	}

	@ModifyConstant(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(stringValue = "Invalid pattern: too many rows, 3 is maximum")
	)
	private static String create$changeHeightWarning(String original) {
		return "Invalid pattern: too many rows, " + HEIGHT + " is maximum";
	}

	@ModifyConstant(method = "patternFromJson(Lcom/google/gson/JsonArray;)[Ljava/lang/String;",
			constant = @Constant(stringValue = "Invalid pattern: too many columns, 3 is maximum")
	)
	private static String create$changeWidthWarning(String original) {
		return "Invalid pattern: too many columns, " + WIDTH + " is maximum";
	}
}
