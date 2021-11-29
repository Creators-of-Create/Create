package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.AllItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
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
