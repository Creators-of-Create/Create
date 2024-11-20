package com.simibubi.create.content.logistics.item.filter.attribute.legacydeserializers;

import com.google.common.collect.ImmutableBiMap;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.function.Supplier;

public class StandardTraitsLegacyDeserializer implements ItemAttribute.LegacyDeserializer {
	@Override
	public String getNBTKey() {
		return "standard_trait";
	}

	@Override
	public ItemAttribute readNBT(CompoundTag nbt) {
		ImmutableBiMap<String, Supplier<ItemAttributeType>> map = ImmutableBiMap.<String, Supplier<ItemAttributeType>>builder()
				.put("PLACEABLE", AllItemAttributeTypes.PLACEABLE)
				.put("CONSUMABLE", AllItemAttributeTypes.CONSUMABLE)
				.put("FLUID_CONTAINER", AllItemAttributeTypes.FLUID_CONTAINER)
				.put("ENCHANTED", AllItemAttributeTypes.ENCHANTED)
				.put("MAX_ENCHANTED", AllItemAttributeTypes.MAX_ENCHANTED)
				.put("RENAMED", AllItemAttributeTypes.RENAMED)
				.put("DAMAGED", AllItemAttributeTypes.DAMAGED)
				.put("BADLY_DAMAGED", AllItemAttributeTypes.BADLY_DAMAGED)
				.put("NOT_STACKABLE", AllItemAttributeTypes.NOT_STACKABLE)
				.put("EQUIPABLE", AllItemAttributeTypes.EQUIPABLE)
				.put("FURNACE_FUEL", AllItemAttributeTypes.FURNACE_FUEL)
				.put("WASHABLE", AllItemAttributeTypes.WASHABLE)
				.put("HAUNTABLE", AllItemAttributeTypes.HAUNTABLE)
				.put("CRUSHABLE", AllItemAttributeTypes.CRUSHABLE)
				.put("SMELTABLE", AllItemAttributeTypes.SMELTABLE)
				.put("SMOKABLE", AllItemAttributeTypes.SMOKABLE)
				.put("BLASTABLE", AllItemAttributeTypes.BLASTABLE)
				.put("COMPOSTABLE", AllItemAttributeTypes.COMPOSTABLE)
				.build();

		for (Map.Entry<String, Supplier<ItemAttributeType>> entry : map.entrySet()) {
			if (nbt.contains(entry.getKey())) {
				return entry.getValue().get().createAttribute();
			}
		}

		throw new IllegalArgumentException("Tried to read standard trait and migrate to a type that doesn't exist!");
	}
}
