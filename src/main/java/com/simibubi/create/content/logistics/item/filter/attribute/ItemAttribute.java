package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ItemAttribute {
	Codec<ItemAttribute> CODEC = CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.byNameCodec().dispatch(ItemAttribute::getType, ItemAttributeType::codec);
	StreamCodec<RegistryFriendlyByteBuf, ItemAttribute> STREAM_CODEC = ByteBufCodecs.registry(CreateRegistries.ITEM_ATTRIBUTE_TYPE).dispatch(ItemAttribute::getType, ItemAttributeType::streamCodec);

	static CompoundTag saveStatic(ItemAttribute attribute, HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("attribute", CatnipCodecUtils.encode(CODEC, registries, attribute).orElseThrow());
		return nbt;
	}

	@Nullable
	static ItemAttribute loadStatic(CompoundTag nbt, HolderLookup.Provider registries) {
		return CatnipCodecUtils.decodeOrNull(CODEC, registries, nbt.get("attribute"));
	}

	static List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
		List<ItemAttribute> attributes = new ArrayList<>();
		for (ItemAttributeType type : CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE) {
			attributes.addAll(type.getAllAttributes(stack, level));
		}
		return attributes;
	}

	boolean appliesTo(ItemStack stack, Level world);

	ItemAttributeType getType();

	@OnlyIn(value = Dist.CLIENT)
	default MutableComponent format(boolean inverted) {
		return CreateLang.translateDirect("item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
			getTranslationParameters());
	}

	String getTranslationKey();

	default Object[] getTranslationParameters() {
		return new String[0];
	}

	record ItemAttributeEntry(ItemAttribute attribute, boolean inverted) {
		public static final Codec<ItemAttributeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
			ItemAttribute.CODEC.fieldOf("attribute").forGetter(ItemAttributeEntry::attribute),
			Codec.BOOL.fieldOf("inverted").forGetter(ItemAttributeEntry::inverted)
		).apply(i, ItemAttributeEntry::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeEntry> STREAM_CODEC = StreamCodec.composite(
			ItemAttribute.STREAM_CODEC, ItemAttributeEntry::attribute,
			ByteBufCodecs.BOOL, ItemAttributeEntry::inverted,
			ItemAttributeEntry::new
		);
	}
}
