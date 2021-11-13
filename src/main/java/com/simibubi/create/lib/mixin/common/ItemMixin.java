package com.simibubi.create.lib.mixin.common;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.ItemExtensions;
import com.simibubi.create.lib.helper.ItemSupplier;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.world.item.Item;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {
	@Unique
	private Supplier<Item> create$supplier;

	@Override
	@Unique
	public Supplier<Item> create$getSupplier() {
		return create$supplier;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void create$item(Item.Properties properties, CallbackInfo ci) {
		create$supplier = new ItemSupplier(MixinHelper.cast(this));
	}
}
