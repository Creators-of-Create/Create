package com.simibubi.create.content.kinetics.belt;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.belt.AllBeltCasingTypes.LegacyCasingType;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BeltCasingType {

	public final Holder.Reference<BeltCasingType> holder = CreateBuiltInRegistries.BELT_CASING_TYPE.createIntrusiveHolder(this);

	private final Supplier<BlockItem> itemSupplier;
	private final Supplier<BeltCasingRenderInfo> renderInfoSupplier;

	public BeltCasingType(BlockEntry<?> casingBlockSupplier, Supplier<BeltCasingRenderInfo> renderInfoSupplier) {
		this(() -> (BlockItem) casingBlockSupplier.get().asItem(), renderInfoSupplier);
	}

	public BeltCasingType(Supplier<BlockItem> casingItemSupplier, Supplier<BeltCasingRenderInfo> renderInfoSupplier) {
		this.itemSupplier = casingItemSupplier;
		this.renderInfoSupplier = renderInfoSupplier;
	}

	/**
	 * Read a BeltCasingType from NBT tag in a way that is compatible with legacy enum IDs.
	 * */
	public static @Nullable BeltCasingType read(CompoundTag tag, String key) {
		if (!tag.contains(key))
			return null;
		String id = tag.getString(key);

		BeltCasingType convertedLegacy = LegacyCasingType.fromId(id);
		if (convertedLegacy != null)
			return convertedLegacy;

		ResourceKey<BeltCasingType> resourceKey = ResourceKey.create(CreateRegistries.BELT_CASING_TYPE, ResourceLocation.parse(id.toLowerCase()));

		return CreateBuiltInRegistries.BELT_CASING_TYPE.getHolder(resourceKey)
			.map(Reference::value)
			.orElse(null);
	}

	public static void write(CompoundTag tag, String key, @Nullable BeltCasingType casingType) {
		if (casingType == null)
			return;
		tag.putString(key, casingType.holder.key().location().toString());
	}

	public BlockItem getCasingBlockItem() {
		return itemSupplier.get();
	}

	public BeltCasingRenderInfo getModelInfo() {
		return renderInfoSupplier.get();
	}

}
