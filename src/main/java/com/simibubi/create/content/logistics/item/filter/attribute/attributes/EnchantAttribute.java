package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantAttribute implements ItemAttribute {
	public static final EnchantAttribute EMPTY = new EnchantAttribute(null);

	private @Nullable Enchantment enchantment;

	public EnchantAttribute(@Nullable Enchantment enchantment) {
		this.enchantment = enchantment;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment);
	}

	@Override
	public String getTranslationKey() {
		return "has_enchant";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (enchantment != null)
			parameter = Components.translatable(enchantment.getDescriptionId()).getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_ENCHANT;
	}

	@Override
	public void save(CompoundTag nbt) {
		if (enchantment == null)
			return;
		ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
		if (id == null)
			return;
		nbt.putString("id", id.toString());
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("id")) {
			enchantment = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(nbt.getString("id")));
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return EMPTY;
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
				list.add(new EnchantAttribute(enchantment));
			}

			return list;
		}
	}
}
