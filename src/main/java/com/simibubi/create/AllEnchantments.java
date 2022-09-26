package com.simibubi.create;

import com.simibubi.create.content.curiosities.armor.BeltWalkerEnchantment;
import com.simibubi.create.content.curiosities.armor.CapacityEnchantment;
import com.simibubi.create.content.curiosities.weapons.PotatoRecoveryEnchantment;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class AllEnchantments {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final RegistryEntry<PotatoRecoveryEnchantment> POTATO_RECOVERY = REGISTRATE.object("potato_recovery")
		.enchantment(EnchantmentCategory.BOW, PotatoRecoveryEnchantment::new)
		.addSlots(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)
		.lang("Potato Recovery")
		.rarity(Rarity.UNCOMMON)
		.register();
	
	public static final RegistryEntry<CapacityEnchantment> CAPACITY = REGISTRATE.object("capacity")
		.enchantment(EnchantmentCategory.ARMOR_CHEST, CapacityEnchantment::new)
		.addSlots(EquipmentSlot.CHEST)
		.lang("Capacity")
		.rarity(Rarity.COMMON)
		.register();

	public static final RegistryEntry<BeltWalkerEnchantment> BELT_WALKER = REGISTRATE.object("belt_walker")
		.enchantment(EnchantmentCategory.ARMOR_FEET, BeltWalkerEnchantment::new)
		.addSlots(EquipmentSlot.FEET)
		.lang("Belt Walker")
		.rarity(Rarity.COMMON)
		.register();

	public static void register() {}

}
