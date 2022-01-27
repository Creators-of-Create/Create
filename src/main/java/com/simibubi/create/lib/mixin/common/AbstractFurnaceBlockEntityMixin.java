package com.simibubi.create.lib.mixin.common;

import net.minecraft.world.item.Item;

import net.minecraft.world.level.ItemLike;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
	@Shadow
	private static void add(Map<Item, Integer> map, ItemLike item, int burnTime) {
		throw new RuntimeException("mixin failed");
	}

	@Inject(method = "getFuel", at = @At("RETURN"), cancellable = true)
	private static void create$getFuel(CallbackInfoReturnable<Map<Item, Integer>> cir) {
		add(cir.getReturnValue(), AllItems.CREATIVE_BLAZE_CAKE.get(), Integer.MAX_VALUE);
	}

	@Inject(method = "getBurnDuration", at = @At("HEAD"), cancellable = true)
	protected void create$getBurnDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		if (itemStack.is(AllItems.CREATIVE_BLAZE_CAKE.get())) {
			cir.setReturnValue(Integer.MAX_VALUE);
		}
	}

	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private static void create$isFuel(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (itemStack.is(AllItems.CREATIVE_BLAZE_CAKE.get())) {
			cir.setReturnValue(true);
		}
	}
}
