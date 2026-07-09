package com.simibubi.create.foundation.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class LegacyBlockEntityDataComponentBridge {
	private LegacyBlockEntityDataComponentBridge() {}

	public static CompoundTag get(ItemStack stack) {
		TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		return data == null ? new CompoundTag() : data.copyTagWithoutId();
	}

	public static void set(ItemStack stack, BlockEntityType<?> type, CompoundTag tag) {
		stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, tag));
	}
}
