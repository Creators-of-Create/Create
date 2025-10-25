package com.simibubi.create.content.equipment.armor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DivingHelmetItem extends BaseArmorItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
	public static final ArmorItem.Type TYPE = ArmorItem.Type.HELMET;

	// TODO - 1.21.1 - Remove
	@Nullable
	private static final MethodHandle setCanRefillAirHandle;

	// TODO - 1.21.1 - Remove
	static {
		MethodHandle handle = null;

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		MethodType type = MethodType.methodType(void.class, boolean.class);
		try {
			handle = lookup.findVirtual(LivingBreatheEvent.class, "setCanRefillAir", type);
		} catch (Exception ignored) {
		}

		setCanRefillAirHandle = handle;
	}

	public DivingHelmetItem(ArmorMaterial material, Properties properties, ResourceLocation textureLoc) {
		super(material, TYPE, properties, textureLoc);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment == Enchantments.AQUA_AFFINITY) {
			return false;
		}
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		if (enchantment == Enchantments.AQUA_AFFINITY) {
			return 1;
		}
		return super.getEnchantmentLevel(stack, enchantment);
	}

	@Override
	public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
		Map<Enchantment, Integer> map = super.getAllEnchantments(stack);
		map.put(Enchantments.AQUA_AFFINITY, 1);
		return map;
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

		if (level.isClientSide)
			entity.getPersistentData().remove("VisualBacktankAir");

		ItemStack helmet = getWornItem(entity);
		if (helmet.isEmpty())
			return;

		boolean lavaDiving = entity.isInLava();
		if (!helmet.getItem().isFireResistant() && lavaDiving)
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
				.noneMatch(backtank -> backtank.getItem()
					.isFireResistant()))
				return;
		}

		float visualBacktankAir = 0f;
		for (ItemStack stack : backtanks)
			visualBacktankAir += BacktankUtil.getAir(stack);

		if (level.isClientSide)
			entity.getPersistentData()
				.putInt("VisualBacktankAir", Math.round(visualBacktankAir));

		if (level.getGameTime() % 20 == 0)
			BacktankUtil.consumeAir(entity, backtanks.get(0), 1);

		if (lavaDiving)
			return;

		if (entity instanceof ServerPlayer sp)
			AllAdvancements.DIVING_SUIT.awardTo(sp);

		event.setCanBreathe(true);

		// TODO - 1.21.1 - Remove
		try {
			if (setCanRefillAirHandle != null)
				setCanRefillAirHandle.invokeExact(event, true);
		} catch (Throwable ignored) {
		}
	}
}
