package com.simibubi.create.content.kinetics.deployer;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ItemApplicationRecipeParams extends ProcessingRecipeParams {
	protected static final List<String> KEYS =
		ImmutableList.<String>builder().addAll(ProcessingRecipeParams.KEYS).add("keep_held_item").build();
	protected static final MapCodec<Boolean> KEEP_HELD_ITEM_CODEC = Codec.BOOL.optionalFieldOf("keep_held_item", false);
	public static MapCodec<ItemApplicationRecipeParams> CODEC = new MapCodec<>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return KEYS.stream().map(ops::createString);
		}

		@Override
		public <T> DataResult<ItemApplicationRecipeParams> decode(DynamicOps<T> ops, MapLike<T> input) {
			return new ItemApplicationRecipeParams().decode(ops, input).map(Function.identity());
		}

		@Override
		public <T> RecordBuilder<T> encode(ItemApplicationRecipeParams input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return input.encode(ops, prefix);
		}
	};
	public static StreamCodec<RegistryFriendlyByteBuf, ItemApplicationRecipeParams> STREAM_CODEC = StreamCodec.of(
		(buffer, params) -> params.encode(buffer),
		buffer -> Util.make(new ItemApplicationRecipeParams(), params -> params.decode(buffer))
	);

	protected boolean keepHeldItem;

	@Override
	protected <T> RecordBuilder<T> encode(DynamicOps<T> ops, RecordBuilder<T> builder) {
		builder = super.encode(ops, builder);
		KEEP_HELD_ITEM_CODEC.encode(keepHeldItem, ops, builder);
		return builder;
	}

	@Override
	protected <T> DataResult<? extends ItemApplicationRecipeParams> decode(DynamicOps<T> ops, MapLike<T> input) {
		var result = super.decode(ops, input);
		if (result.isError()) return result.map(it -> this);

		var keepHeldItem = KEEP_HELD_ITEM_CODEC.decode(ops, input);
		if (keepHeldItem.isError()) return result.map(it -> this);
		this.keepHeldItem = keepHeldItem.getOrThrow();
		return DataResult.success(this);
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
