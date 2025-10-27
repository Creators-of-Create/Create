package com.simibubi.create;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.SetValue;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class AllEnchantments {
	public static final ResourceKey<Enchantment>
			POTATO_RECOVERY = key("potato_recovery"),
			CAPACITY = key("capacity");

	private static ResourceKey<Enchantment> key(String name) {
		return ResourceKey.create(Registries.ENCHANTMENT, Create.asResource(name));
	}

	public static void bootstrap(BootstrapContext<Enchantment> context) {
		HolderGetter<Item> itemHolderGetter = context.lookup(Registries.ITEM);

		register(
				context,
				POTATO_RECOVERY,
				Enchantment.enchantment(
						Enchantment.definition(
								HolderSet.direct(AllItems.POTATO_CANNON),
								10,
								3,
								Enchantment.dynamicCost(15, 15),
								Enchantment.dynamicCost(45, 15),
								1,
								EquipmentSlotGroup.MAINHAND
						)
				).withEffect(
						EnchantmentEffectComponents.AMMO_USE,
						new SetValue(LevelBasedValue.perLevel(0.0F, 33.3333333333F)),
						MatchTool.toolMatches(
								ItemPredicate.Builder.item().of() // TODO - Fix potato recovery
						)
				)
		);

		register(
				context,
				CAPACITY,
				Enchantment.enchantment(
						Enchantment.definition(
								itemHolderGetter.getOrThrow(AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.tag),
								10,
								3,
								Enchantment.dynamicCost(15, 15),
								Enchantment.dynamicCost(45, 15),
								1,
								EquipmentSlotGroup.MAINHAND
						)
				)
		);
	}

	private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
		context.register(key, builder.build(key.location()));
	}

}
