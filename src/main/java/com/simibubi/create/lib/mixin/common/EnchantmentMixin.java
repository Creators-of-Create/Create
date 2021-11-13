package com.simibubi.create.lib.mixin.common;

import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.utility.EnchantmentUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
	@Inject(at = @At("HEAD"), method = "canEnchant", cancellable = true)
	private void canEnchant(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		Set<Supplier<Enchantment>> enchants = EnchantmentUtil.ITEMS_TO_ENCHANTS.get(itemStack.getItem());
		if (enchants != null) {
			for (Supplier<Enchantment> enchant : enchants) {
				if (enchant.get() == (Object) this) {
					cir.setReturnValue(true);
				}
			}
		}
	}
}
