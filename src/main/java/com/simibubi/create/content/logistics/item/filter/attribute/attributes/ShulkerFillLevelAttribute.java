package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class ShulkerFillLevelAttribute implements ItemAttribute {
	private ShulkerLevels levels;

	public ShulkerFillLevelAttribute(ShulkerLevels levels) {
		this.levels = levels;
	}

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
			parameter = Lang.translateDirect("item_attributes." + getTranslationKey() + "." + levels.key).getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.SHULKER_FILL_LEVEL.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		if (levels != null)
			nbt.putString("id", levels.key);
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("id")) {
			levels = ShulkerLevels.fromKey(nbt.getString("id"));
		}
	}

	enum ShulkerLevels implements StringRepresentable {
		EMPTY("empty", amount -> amount == 0),
		PARTIAL("partial", amount -> amount > 0 && amount < Integer.MAX_VALUE),
		FULL("full", amount -> amount == Integer.MAX_VALUE);

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
			CompoundTag compoundnbt = testStack.getTagElement("BlockEntityTag");
			if (compoundnbt == null)
				return requiredSize.test(0);
			if (compoundnbt.contains("LootTable", 8))
				return false;
			if (compoundnbt.contains("Items", 9)) {
				int rawSize = compoundnbt.getList("Items", 10).size();
				if (rawSize < 27)
					return requiredSize.test(rawSize);

				NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
				ContainerHelper.loadAllItems(compoundnbt, inventory);
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
	}
}
