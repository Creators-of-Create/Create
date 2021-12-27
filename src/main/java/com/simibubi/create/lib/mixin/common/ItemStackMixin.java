package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.extensions.ItemStackExtensions;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.item.CustomMaxCountItem;
import com.simibubi.create.lib.util.MixinHelper;
import com.simibubi.create.lib.util.NBTSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements NBTSerializable, ItemStackExtensions {
	@Shadow
	@Final
	@Mutable
	@Deprecated
	private Item item;

	@Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
	public void create$onGetMaxCount(CallbackInfoReturnable<Integer> cir) {
		ItemStack self = (ItemStack) (Object) this;
		Item item = self.getItem();
		if (item instanceof CustomMaxCountItem) {
			cir.setReturnValue(((CustomMaxCountItem) item).getItemStackLimit(self));
		}
	}

	@Override
	public CompoundTag create$serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		MixinHelper.<ItemStack>cast(this).save(nbt);
		return nbt;
	}

	@Override
	public void create$deserializeNBT(CompoundTag nbt) {
		MixinHelper.<ItemStack>cast(this).setTag(ItemStack.of(nbt).getTag());
	}

	@Override
	public void setItem(Item item) {
		this.item = item;
	}
}
