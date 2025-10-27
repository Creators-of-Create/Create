package com.simibubi.create.content.logistics.stockTicker;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PackageOrder(List<BigItemStack> stacks) {
	public static final Codec<PackageOrder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.list(BigItemStack.CODEC).fieldOf("entries").forGetter(PackageOrder::stacks)
	).apply(instance, PackageOrder::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PackageOrder> STREAM_CODEC = CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC)
		.map(PackageOrder::new, PackageOrder::stacks);

	public static PackageOrder empty() {
		return new PackageOrder(Collections.emptyList());
	}

	public boolean isEmpty() {
		return stacks.isEmpty();
	}
}
