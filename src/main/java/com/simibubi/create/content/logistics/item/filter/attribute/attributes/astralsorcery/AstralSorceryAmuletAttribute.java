package com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralSorceryAmuletAttribute implements ItemAttribute {
	String enchName;
	int enchType;

	public AstralSorceryAmuletAttribute(String enchName, int enchType) {
		this.enchName = enchName;
		this.enchType = enchType;
	}

	private static ListTag extractTraitList(ItemStack stack) {
		return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getList("amuletEnchantments", 10) : new ListTag();
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		for (Tag trait : extractTraitList(itemStack)) {
			if (((CompoundTag) trait).getString("ench").equals(this.enchName)
					&& ((CompoundTag) trait).getInt("type") == this.enchType)
				return true;
		}
		return false;
	}

	@Override
	public String getTranslationKey() {
		return "astralsorcery_amulet";
	}

	@Override
	public Object[] getTranslationParameters() {
		String something = "";

		Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(enchName));
		if (enchant != null) {
			something = Components.translatable(enchant.getDescriptionId()).getString();
		}

		if (enchType == 1) something = "existing " + something;

		return new Object[]{something};
	}

	@Override
	public ItemAttributeType getType() {
		return null;
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("enchName", enchName);
		nbt.putInt("enchType", enchType);
	}

	@Override
	public void load(CompoundTag nbt) {
		enchName = nbt.getString("enchName");
		enchType = nbt.getInt("enchType");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AstralSorceryAmuletAttribute("dummy", -1);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack itemStack, Level level) {
			ListTag traits = extractTraitList(itemStack);
			List<ItemAttribute> atts = new ArrayList<>();
			for (int i = 0; i < traits.size(); i++) {
				atts.add(new AstralSorceryAmuletAttribute(
						traits.getCompound(i).getString("ench"),
						traits.getCompound(i).getInt("type")));
			}
			return atts;
		}
	}
}
