package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.curiosities.armor.DivingHelmetItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@Inject(method = "canEnchant(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void onCanEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this == Enchantments.AQUA_AFFINITY && stack.getItem() instanceof DivingHelmetItem) {
			cir.setReturnValue(false);
		}
	}
}
