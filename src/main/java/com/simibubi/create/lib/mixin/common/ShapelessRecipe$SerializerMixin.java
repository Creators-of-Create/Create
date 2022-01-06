package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.util.ShapedRecipeUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.item.crafting.ShapelessRecipe;

@Mixin(ShapelessRecipe.Serializer.class)
public abstract class ShapelessRecipe$SerializerMixin {
	@ModifyConstant(
			method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/ShapelessRecipe;",
			constant = @Constant(intValue = 9)
	)
	private static int create$modifyMaxItemsInRecipe(int original) {
		return ShapedRecipeUtil.HEIGHT * ShapedRecipeUtil.WIDTH;
	}
}
