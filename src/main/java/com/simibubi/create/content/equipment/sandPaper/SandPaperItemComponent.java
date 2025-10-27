package com.simibubi.create.content.equipment.sandPaper;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SandPaperItemComponent(ItemStack item) {

	public static final Codec<SandPaperItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(ItemStack.OPTIONAL_CODEC.fieldOf("item")
			.forGetter(i -> i.item))
		.apply(instance, SandPaperItemComponent::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SandPaperItemComponent> STREAM_CODEC =
		StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, i -> i.item, SandPaperItemComponent::new);

	@Override
	public final boolean equals(Object arg0) {
		return arg0 instanceof ItemStack otherItem && ItemStack.isSameItemSameComponents(otherItem, item);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(item.getItem(), item.getCount(), item.getComponents());
	}
	
}
