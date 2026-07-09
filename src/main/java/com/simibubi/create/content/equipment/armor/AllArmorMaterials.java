package com.simibubi.create.content.equipment.armor;

import java.util.Map;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllArmorMaterials {
	public static final ArmorMaterial COPPER = register(
				"copper",
				new int[] { 2, 4, 3, 1, 4 },
				7,
				AllSoundEvents.COPPER_ARMOR_EQUIP.getMainEventHolder(),
				0.0F,
				0.0F,
				ItemTags.REPAIRS_COPPER_ARMOR
			);

	public static final ArmorMaterial CARDBOARD = register(
				"cardboard",
				new int[] { 1, 1, 1, 1, 2 },
				4,
				SoundEvents.ARMOR_EQUIP_LEATHER,
				0.0F,
				0.0F,
				AllItemTags.CARDBOARD_PLATES.tag
	);

	private static ArmorMaterial register(
			String name,
			int[] defense,
			int enchantmentValue,
			net.minecraft.core.Holder<SoundEvent> equipSound,
			float toughness,
			float knockbackResistance,
			TagKey<Item> repairIngredient
	) {
		Map<ArmorType, Integer> defenseMap = Maps.newEnumMap(Map.of(
			ArmorType.BOOTS, defense[0],
			ArmorType.LEGGINGS, defense[1],
			ArmorType.CHESTPLATE, defense[2],
			ArmorType.HELMET, defense[3],
			ArmorType.BODY, defense[4]
		));
		ResourceKey<EquipmentAsset> assetId = ResourceKey.create(EquipmentAssets.ROOT_ID, Create.asResource(name));
		return new ArmorMaterial(enchantmentValue, defenseMap, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, assetId);
	}

	@Internal
	public static void register(IEventBus eventBus) {
	}
}
