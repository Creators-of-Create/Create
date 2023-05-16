package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.curiosities.armor.DivingHelmetItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
	@Inject(method = "getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
	private static void create$onGetItemEnchantmentLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (enchantment == Enchantments.AQUA_AFFINITY && stack.getItem() instanceof DivingHelmetItem) {
			cir.setReturnValue(1);
		}
	}
}
