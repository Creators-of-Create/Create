package com.simibubi.create.content.equipment.armor;

import java.util.List;

import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

@EventBusSubscriber
public class DivingHelmetItem extends BaseArmorItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
	public static final ArmorType TYPE = ArmorType.HELMET;

	public DivingHelmetItem(ArmorMaterial material, Properties properties, Identifier textureLoc) {
		super(material, TYPE, properties, textureLoc);
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.AQUA_AFFINITY))
			return false;
		return super.supportsEnchantment(stack, enchantment);
	}

	@Override
	public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.AQUA_AFFINITY))
			return 1;
		return super.getEnchantmentLevel(stack, enchantment);
	}

	@Override
	public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
		ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(super.getAllEnchantments(stack, lookup));
		enchants.set(lookup.getOrThrow(Enchantments.AQUA_AFFINITY), 1);
		return enchants.toImmutable();
	}

	public static boolean isWornBy(Entity entity) {
		return !getWornItem(entity).isEmpty();
	}

	public static ItemStack getWornItem(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = livingEntity.getItemBySlot(SLOT);
		if (!(stack.getItem() instanceof DivingHelmetItem)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	@SubscribeEvent
	public static void breatheUnderwater(LivingBreatheEvent event) {
		LivingEntity entity = event.getEntity();
		Level level = entity.level();

		if (level.isClientSide())
			entity.getPersistentData().remove("VisualBacktankAir");

		ItemStack helmet = getWornItem(entity);
		if (helmet.isEmpty())
			return;

		boolean lavaDiving = entity.isInLava();
		if (!isFireResistant(helmet, entity) && lavaDiving)
			return;

		if (event.canBreathe() && !lavaDiving)
			return;

		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(entity);
		if (backtanks.isEmpty())
			return;

		if (lavaDiving) {
			if (entity instanceof ServerPlayer sp)
				AllAdvancements.DIVING_SUIT_LAVA.awardTo(sp);
			if (backtanks.stream()
				.noneMatch(backtank -> isFireResistant(backtank, entity)))
				return;
		}

		float visualBacktankAir = 0f;
		for (ItemStack stack : backtanks)
			visualBacktankAir += BacktankUtil.getAir(stack);

		if (level.isClientSide())
			entity.getPersistentData()
				.putInt("VisualBacktankAir", Math.round(visualBacktankAir));

		if (level.getGameTime() % 20 == 0)
			BacktankUtil.consumeAir(entity, backtanks.get(0), 1);

		if (lavaDiving)
			return;

		if (entity instanceof ServerPlayer sp)
			AllAdvancements.DIVING_SUIT.awardTo(sp);

		event.setCanBreathe(true);
		event.setRefillAirAmount(entity.getMaxAirSupply());
	}

	private static boolean isFireResistant(ItemStack stack, LivingEntity entity) {
		var resistance = stack.get(DataComponents.DAMAGE_RESISTANT);
		return resistance != null && resistance.isResistantTo(entity.damageSources().lava());
	}
}
