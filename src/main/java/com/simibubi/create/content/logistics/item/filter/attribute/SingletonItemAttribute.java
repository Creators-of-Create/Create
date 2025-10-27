package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public final class SingletonItemAttribute implements ItemAttribute {
	private final Type type;
	private final BiPredicate<ItemStack, Level> predicate;
	private final String translationKey;

	public SingletonItemAttribute(Type type, BiPredicate<ItemStack, Level> predicate, String translationKey) {
		this.type = type;
		this.predicate = predicate;
		this.translationKey = translationKey;
	}

	@Override
	public boolean appliesTo(ItemStack stack, Level world) {
		return predicate.test(stack, world);
	}

	@Override
	public ItemAttributeType getType() {
		return type;
	}

	@Override
	public String getTranslationKey() {
		return translationKey;
	}

	public static final class Type implements ItemAttributeType {
		private final SingletonItemAttribute attribute;

		public Type(Function<Type, SingletonItemAttribute> singletonFunc) {
			this.attribute = singletonFunc.apply(this);
		}

		@Override
		public @NotNull ItemAttribute createAttribute() {
			return attribute;
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			if (attribute.appliesTo(stack, level)) {
				return List.of(attribute);
			}
			return List.of();
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return Codec.unit(attribute).fieldOf("value");
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
			return StreamCodec.unit(attribute);
		}
	}
}
