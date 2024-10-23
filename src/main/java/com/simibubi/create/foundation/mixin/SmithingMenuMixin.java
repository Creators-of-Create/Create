package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.armor.BacktankItem;

import net.minecraft.world.inventory.SmithingMenu;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {
	// Only add enchantments to the backtank if it supports them
	@ModifyExpressionValue(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/crafting/SmithingRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"
			)
	)
	private ItemStack create$preventUnbreakingOnBacktanks(ItemStack original) {
		if (AllItems.COPPER_BACKTANK.is(original) || AllItems.NETHERITE_BACKTANK.is(original)) {
			Map<Enchantment, Integer> enchantments = new HashMap<>();

			EnchantmentHelper.getEnchantments(original).forEach((enchantment, level) -> {
				if (enchantment.canEnchant(original))
					enchantments.put(enchantment, level);
			});

			EnchantmentHelper.setEnchantments(enchantments, original);
		}

		return original;
	}
}
