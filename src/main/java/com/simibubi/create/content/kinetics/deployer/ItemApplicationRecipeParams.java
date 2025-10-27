package com.simibubi.create.content.kinetics.deployer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ItemApplicationRecipeParams extends ProcessingRecipeParams {
	public static MapCodec<ItemApplicationRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		codec(ItemApplicationRecipeParams::new).forGetter(Function.identity()),
		Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipeParams::keepHeldItem)
	).apply(instance, (params, keepHeldItem) -> {
		params.keepHeldItem = keepHeldItem;
		return params;
	}));
	public static StreamCodec<RegistryFriendlyByteBuf, ItemApplicationRecipeParams> STREAM_CODEC = streamCodec(ItemApplicationRecipeParams::new);

	protected boolean keepHeldItem;

	protected final boolean keepHeldItem() {
		return keepHeldItem;
	}

	@Override
	protected void encode(RegistryFriendlyByteBuf buffer) {
		super.encode(buffer);
		ByteBufCodecs.BOOL.encode(buffer, keepHeldItem);
	}

	@Override
	protected void decode(RegistryFriendlyByteBuf buffer) {
		super.decode(buffer);
		keepHeldItem = ByteBufCodecs.BOOL.decode(buffer);
	}
}
