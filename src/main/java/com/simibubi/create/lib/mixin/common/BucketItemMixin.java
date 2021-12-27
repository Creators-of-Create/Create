package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.transfer.fluid.FluidStorageHandlerItem;

import net.minecraft.world.item.BucketItem;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BucketItem.class)
public class BucketItemMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void create$grabContents(Fluid fluid, Properties properties, CallbackInfo ci) {
		FluidStorageHandlerItem.BUCKETS.put(fluid, (BucketItem) (Object) this);
	}
}
