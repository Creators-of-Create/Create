package com.simibubi.create.content.logistics.item.filter.attribute.attributes.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralSorceryAttunementAttribute implements ItemAttribute {
	String constellationName;

	public AstralSorceryAttunementAttribute(String constellationName) {
		this.constellationName = constellationName;
	}

	private static CompoundTag extractAstralNBT(ItemStack stack) {
		return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery") : new CompoundTag();
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		CompoundTag nbt = extractAstralNBT(itemStack);
		String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

		// Special handling for shifting stars
		ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
		if (itemResource != null && itemResource.toString().contains("shifting_star_")) {
			constellation = itemResource.toString().replace("shifting_star_", "");
		}

		return constellation.equals(constellationName);
	}

	@Override
	public String getTranslationKey() {
		return "astralsorcery_constellation";
	}

	@Override
	public Object[] getTranslationParameters() {
		ResourceLocation constResource = new ResourceLocation(constellationName);
		String something = Components.translatable(String.format("%s.constellation.%s", constResource.getNamespace(), constResource.getPath())).getString();
		return new Object[]{something};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.ASTRAL_ATTUNMENT;
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("constellation", constellationName);
	}

	@Override
	public void load(CompoundTag nbt) {
		constellationName = nbt.getString("constellation");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AstralSorceryAttunementAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack itemStack, Level level) {
			CompoundTag nbt = extractAstralNBT(itemStack);
			String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

			// Special handling for shifting stars
			ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
			if (itemResource != null && itemResource.toString().contains("shifting_star_")) {
				constellation = itemResource.toString().replace("shifting_star_", "");
			}

			List<ItemAttribute> atts = new ArrayList<>();
			if (constellation.length() > 0) {
				atts.add(new AstralSorceryAttunementAttribute(constellation));
			}
			return atts;
		}
	}
}
