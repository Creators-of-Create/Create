package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.CreateLang;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public record ShulkerFillLevelAttribute(ShulkerLevels levels) implements ItemAttribute {
	public static final MapCodec<ShulkerFillLevelAttribute> CODEC = ShulkerLevels.CODEC
			.xmap(ShulkerFillLevelAttribute::new, ShulkerFillLevelAttribute::levels)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, ShulkerFillLevelAttribute> STREAM_CODEC = ShulkerLevels.STREAM_CODEC
		.map(ShulkerFillLevelAttribute::new, ShulkerFillLevelAttribute::levels);

	@Override
	public boolean appliesTo(ItemStack stack, Level level) {
		return levels != null && levels.canApply(stack);
	}

	@Override
	public String getTranslationKey() {
		return "shulker_level";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (levels != null)
			parameter = CreateLang.translateDirect("item_attributes." + getTranslationKey() + "." + levels.key).getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.SHULKER_FILL_LEVEL;
	}

	enum ShulkerLevels implements StringRepresentable {
		EMPTY("empty", amount -> amount == 0),
		PARTIAL("partial", amount -> amount > 0 && amount < Integer.MAX_VALUE),
		FULL("full", amount -> amount == Integer.MAX_VALUE);

		public static final Codec<ShulkerLevels> CODEC = StringRepresentable.fromValues(ShulkerLevels::values);
		public static final StreamCodec<ByteBuf, ShulkerLevels> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ShulkerLevels.class);

		private final Predicate<Integer> requiredSize;
		private final String key;

		ShulkerLevels(String key, Predicate<Integer> requiredSize) {
			this.key = key;
			this.requiredSize = requiredSize;
		}

		@Nullable
		public static ShulkerFillLevelAttribute.ShulkerLevels fromKey(String key) {
			return Arrays.stream(values()).filter(shulkerLevels -> shulkerLevels.key.equals(key)).findFirst().orElse(null);
		}

		private static boolean isShulker(ItemStack stack) {
			return Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock;
		}

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}

		public boolean canApply(ItemStack testStack) {
			if (!isShulker(testStack))
				return false;
			ItemContainerContents contents = testStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
			if (contents == ItemContainerContents.EMPTY)
				return requiredSize.test(0);
			if (testStack.has(DataComponents.CONTAINER_LOOT))
				return false;
			if (contents.getSlots() > 0) {
				int rawSize = contents.getSlots();
				if (rawSize < 27)
					return requiredSize.test(rawSize);

				NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
				contents.copyInto(inventory);
				boolean isFull = inventory.stream().allMatch(itemStack -> !itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxStackSize());
				return requiredSize.test(isFull ? Integer.MAX_VALUE : rawSize);
			}
			return requiredSize.test(0);
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new ShulkerFillLevelAttribute(null);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (ShulkerLevels shulkerLevels : ShulkerLevels.values()) {
				if (shulkerLevels.canApply(stack)) {
					list.add(new ShulkerFillLevelAttribute(shulkerLevels));
				}
			}

			return list;
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
