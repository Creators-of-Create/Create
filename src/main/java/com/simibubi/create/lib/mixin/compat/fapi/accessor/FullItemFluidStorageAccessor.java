package com.simibubi.create.lib.mixin.compat.fapi.accessor;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FullItemFluidStorage.class)
public interface FullItemFluidStorageAccessor {
	@Accessor("fullToEmptyMapping")
	Function<ItemVariant, ItemVariant> create$fullToEmptyMapping();

	@Accessor("fullItem")
	Item create$fullItem();
}
