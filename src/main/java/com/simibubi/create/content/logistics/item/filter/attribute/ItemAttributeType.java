package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ItemAttributeType {
	@NotNull ItemAttribute createAttribute();

	List<ItemAttribute> getAllAttributes(ItemStack stack, Level level);

	MapCodec<? extends ItemAttribute> codec();

	StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec();
}
