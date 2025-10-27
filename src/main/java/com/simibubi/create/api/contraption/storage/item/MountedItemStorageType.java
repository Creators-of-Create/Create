package com.simibubi.create.api.contraption.storage.item;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.impl.contraption.storage.MountedItemStorageFallbackProvider;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MountedItemStorageType<T extends MountedItemStorage> {
	public static final Codec<MountedItemStorageType<?>> CODEC = CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE.byNameCodec();
	public static final SimpleRegistry<Block, MountedItemStorageType<?>> REGISTRY = Util.make(() -> {
		SimpleRegistry<Block, MountedItemStorageType<?>> registry = SimpleRegistry.create();
		registry.registerProvider(MountedItemStorageFallbackProvider.INSTANCE);
		return registry;
	});

	public final MapCodec<? extends T> codec;
	public final Holder.Reference<MountedItemStorageType<?>> holder = CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE.createIntrusiveHolder(this);

	protected MountedItemStorageType(MapCodec<? extends T> codec) {
		this.codec = codec;
	}

	public final boolean is(TagKey<MountedItemStorageType<?>> tag) {
		return this.holder.is(tag);
	}

	@Nullable
	public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);

	/**
	 * Utility for use with Registrate builders. Creates a builder transformer
	 * that will register the given MountedItemStorageType to a block when ready.
	 */
	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedItemStorage(RegistryEntry<MountedItemStorageType<?>, ? extends MountedItemStorageType<?>> type) {
		return builder -> builder.onRegisterAfter(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, block -> REGISTRY.register(block, type.get()));
	}
}
