package com.simibubi.create.lib.mixin.compat.fapi.accessor;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@Mixin(EmptyItemFluidStorage.class)
public interface EmptyItemFluidStorageAccessor {
	@Accessor("emptyToFullMapping")
	Function<ItemVariant, ItemVariant> create$emptyToFullMapping();

	@Accessor("emptyItem")
	Item create$emptyItem();
}
