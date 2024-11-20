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

public class AstralSorceryCrystalAttribute implements ItemAttribute {
	String traitName;

	public AstralSorceryCrystalAttribute(String traitName) {
		this.traitName = traitName;
	}

	private static ListTag extractTraitList(ItemStack stack) {
		return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getCompound("crystalProperties").getList("attributes", 10) : new ListTag();
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		for (Tag trait : extractTraitList(itemStack)) {
			if (((CompoundTag) trait).getString("property").equals(this.traitName))
				return true;
		}
		return false;
	}

	@Override
	public String getTranslationKey() {
		return "astralsorcery_crystal";
	}

	@Override
	public Object[] getTranslationParameters() {
		ResourceLocation traitResource = new ResourceLocation(traitName);
		String something = Components.translatable(String.format("crystal.property.%s.%s.name", traitResource.getNamespace(), traitResource.getPath())).getString();
		return new Object[]{something};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.ASTRAL_CRYSTAL.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("property", traitName);
	}

	@Override
	public void load(CompoundTag nbt) {
		traitName = nbt.getString("property");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AstralSorceryCrystalAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack itemStack, Level level) {
			ListTag traits = extractTraitList(itemStack);
			List<ItemAttribute> atts = new ArrayList<>();
			for (int i = 0; i < traits.size(); i++) {
				atts.add(new AstralSorceryCrystalAttribute(traits.getCompound(i).getString("property")));
			}
			return atts;
		}
	}
}
