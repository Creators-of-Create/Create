package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record InTagAttribute(TagKey<Item> tag) implements ItemAttribute {
	public static final MapCodec<InTagAttribute> CODEC = TagKey.codec(Registries.ITEM)
			.xmap(InTagAttribute::new, InTagAttribute::tag)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, InTagAttribute> STREAM_CODEC = CatnipStreamCodecBuilders.tagKey(Registries.ITEM)
		.map(InTagAttribute::new, InTagAttribute::tag);

	@Override
	public boolean appliesTo(ItemStack stack, Level level) {
		return stack.is(tag);
	}

	@Override
	public String getTranslationKey() {
		return "in_tag";
	}

	@Override
	public Object[] getTranslationParameters() {
		String fallback = "#" + tag.location();
		if (AllConfigs.client().translateTags.get()) {
			String tagKey = "tag.item." + tag.location().getNamespace() + "." + tag.location().getPath().replace('/', '.');
			if (I18n.exists(tagKey))
				return new Object[] { "'" + I18n.get(tagKey) + "'" };
		}
		return new Object[] { fallback };
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.IN_TAG;
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new InTagAttribute(ItemTags.LOGS);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			return stack.getTags()
				.map(InTagAttribute::new)
				.collect(Collectors.toList());
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
