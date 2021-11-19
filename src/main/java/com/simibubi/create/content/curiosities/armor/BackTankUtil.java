package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;

import com.simibubi.create.lib.item.CustomDurabilityBarItem;

import com.simibubi.create.lib.utility.DurabilityBarUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BackTankUtil {

	public static ItemStack get(LivingEntity entity) {
		for (ItemStack itemStack : entity.getArmorSlots())
			if (AllItems.COPPER_BACKTANK.isIn(itemStack))
				return itemStack;
		return ItemStack.EMPTY;
	}

	public static boolean hasAirRemaining(ItemStack backtank) {
		return getAir(backtank) > 0;
	}

	public static float getAir(ItemStack backtank) {
		CompoundTag tag = backtank.getOrCreateTag();
		return Math.min(tag.getFloat("Air"), maxAir(backtank));
	}

	public static void consumeAir(ItemStack backtank, float i) {
		CompoundTag tag = backtank.getOrCreateTag();
		tag.putFloat("Air", Math.min(getAir(backtank) - i, maxAir(backtank)));
		backtank.setTag(tag);
	}

	public static int maxAir(ItemStack backtank) {
		return maxAir(EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), backtank));
	}

	public static int maxAir(int enchantLevel) {
		return AllConfigs.SERVER.curiosities.airInBacktank.get()
			+ AllConfigs.SERVER.curiosities.enchantedBacktankCapacity.get() * enchantLevel;
	}

	public static int maxAirWithoutEnchants() {
		return AllConfigs.SERVER.curiosities.airInBacktank.get();
	}

	public static boolean canAbsorbDamage(LivingEntity entity, int usesPerTank) {
		if (usesPerTank == 0)
			return true;
		if (entity instanceof Player && ((Player) entity).isCreative())
			return true;
		ItemStack backtank = get(entity);
		if (backtank.isEmpty())
			return false;
		if (!hasAirRemaining(backtank))
			return false;
		float cost = ((float) maxAirWithoutEnchants()) / usesPerTank;
		consumeAir(backtank, cost);
		return true;
	}

	// For Air-using tools

	@Environment(EnvType.CLIENT)
	public static int getRGBDurabilityForDisplay(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return 0;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return Mth.hsvToRgb(
				Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack, usesPerTank))) / 3.0F, 1.0F, 1.0F);
		int color;
		if (backtank.getItem() instanceof CustomDurabilityBarItem custom) {
			color = custom.getRGBDurabilityForDisplay(backtank);
		} else {
			color = backtank.getItem().getBarColor(backtank);
		}
		return color;
	}

	@Environment(EnvType.CLIENT)
	public static double getDurabilityForDisplay(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return 0;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return (double) stack.getDamageValue() / (double) stack.getMaxDamage();
		double durability;
		if (backtank.getItem() instanceof CustomDurabilityBarItem custom) {
			durability = custom.getDurabilityForDisplay(backtank);
		} else {
			durability = DurabilityBarUtil.getDurabilityForDisplay(backtank);
		}
		return durability;
	}

	@Environment(EnvType.CLIENT)
	public static boolean showDurabilityBar(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return false;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return false;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return stack.isDamaged();
		return true;
	}

}
