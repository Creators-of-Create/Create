package com.simibubi.create.content.equipment.armor;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllArmorMaterials {
	private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Create.ID);

	public static final Holder<ArmorMaterial> COPPER = register(
				"copper",
				new int[] { 2, 4, 3, 1, 4 },
				7,
				AllSoundEvents.COPPER_ARMOR_EQUIP.getMainEventHolder(),
				0.0F,
				0.0F,
				() -> Ingredient.of(Items.COPPER_INGOT)
			);

	public static final Holder<ArmorMaterial> CARDBOARD = register(
				"cardboard",
				new int[] { 1, 1, 1, 1, 2 },
				4,
				SoundEvents.ARMOR_EQUIP_LEATHER,
				0.0F,
				0.0F,
				() -> Ingredient.of(AllItems.CARDBOARD)
	);

	private static Holder<ArmorMaterial> register(
			String name,
			int[] defense,
			int enchantmentValue,
			Holder<SoundEvent> equipSound,
			float toughness,
			float knockbackResistance,
			Supplier<Ingredient> repairIngredient
	) {
		List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(Create.asResource(name)));
		return register(name, defense, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, list);
	}

	private static Holder<ArmorMaterial> register(
			String name,
			int[] defense,
			int enchantmentValue,
			Holder<SoundEvent> equipSound,
			float toughness,
			float knockbackResistance,
			Supplier<Ingredient> repairIngridient,
			List<ArmorMaterial.Layer> layers
	) {
		EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

		for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
			enummap.put(armoritem$type, defense[armoritem$type.ordinal()]);
		}

		return ARMOR_MATERIALS.register(name,
				() -> new ArmorMaterial(enummap, enchantmentValue, equipSound, repairIngridient, layers, toughness, knockbackResistance)
		);
	}

	@Internal
	public static void register(IEventBus eventBus) {
		ARMOR_MATERIALS.register(eventBus);
	}
}
