package com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AstralSorceryPerkGemAttribute implements ItemAttribute {
	String traitName;

	public AstralSorceryPerkGemAttribute(String traitName) {
		this.traitName = traitName;
	}

	private static ListTag extractTraitList(ItemStack stack) {
		return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getList("attribute_modifiers", 10) : new ListTag();
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		for (Tag trait : extractTraitList(itemStack)) {
			if (((CompoundTag) trait).getString("type").equals(this.traitName))
				return true;
		}
		return false;
	}

	@Override
	public String getTranslationKey() {
		return "astralsorcery_perk_gem";
	}

	@Override
	public Object[] getTranslationParameters() {
		ResourceLocation traitResource = new ResourceLocation(traitName);
		String something = Components.translatable(String.format("perk.attribute.%s.%s.name", traitResource.getNamespace(), traitResource.getPath())).getString();
		return new Object[]{something};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.ASTRAL_PERK_GEM.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("type", traitName);
	}

	@Override
	public void load(CompoundTag nbt) {
		traitName = nbt.getString("type");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AstralSorceryPerkGemAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack itemStack, Level level) {
			ListTag traits = extractTraitList(itemStack);
			List<ItemAttribute> atts = new ArrayList<>();
			for (int i = 0; i < traits.size(); i++) {
				atts.add(new AstralSorceryPerkGemAttribute(traits.getCompound(i).getString("type")));
			}
			return atts;
		}
	}
}
