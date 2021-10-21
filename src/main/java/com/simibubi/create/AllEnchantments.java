package com.simibubi.create;

import com.simibubi.create.content.curiosities.armor.CapacityEnchantment;
import com.simibubi.create.content.curiosities.weapons.PotatoRecoveryEnchantment;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class AllEnchantments {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final RegistryEntry<PotatoRecoveryEnchantment> POTATO_RECOVERY = REGISTRATE.object("potato_recovery")
		.enchantment(EnchantmentType.BOW, PotatoRecoveryEnchantment::new)
		.addSlots(EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND)
		.lang("Potato Recovery")
		.rarity(Rarity.UNCOMMON)
		.register();
	
	public static final RegistryEntry<CapacityEnchantment> CAPACITY = REGISTRATE.object("capacity")
		.enchantment(EnchantmentType.ARMOR_CHEST, CapacityEnchantment::new)
		.addSlots(EquipmentSlotType.CHEST)
		.lang("Capacity")
		.rarity(Rarity.COMMON)
		.register();

	public static void register() {}

}
