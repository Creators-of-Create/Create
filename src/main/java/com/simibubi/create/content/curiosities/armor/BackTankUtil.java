package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BackTankUtil {

	public static ItemStack get(LivingEntity entity) {
		for (ItemStack itemStack : entity.getArmorInventoryList())
			if (AllItems.COPPER_BACKTANK.isIn(itemStack))
				return itemStack;
		return ItemStack.EMPTY;
	}

	public static boolean hasAirRemaining(ItemStack backtank) {
		return getAir(backtank) > 0;
	}

	public static float getAir(ItemStack backtank) {
		CompoundNBT tag = backtank.getOrCreateTag();
		return tag.getFloat("Air");
	}

	public static void consumeAir(ItemStack backtank, float i) {
		CompoundNBT tag = backtank.getOrCreateTag();
		tag.putFloat("Air", getAir(backtank) - i);
		backtank.setTag(tag);
	}

	private static float maxAir() {
		return AllConfigs.SERVER.curiosities.maxAirInBacktank.get();
	}

	public static boolean canAbsorbDamage(LivingEntity entity, int usesPerTank) {
		if (usesPerTank == 0)
			return true;
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())
			return true;
		ItemStack backtank = get(entity);
		if (backtank.isEmpty())
			return false;
		if (!hasAirRemaining(backtank))
			return false;
		float cost = maxAir() / usesPerTank;
		consumeAir(backtank, cost);
		return true;
	}

	// For Air-using tools

	@OnlyIn(Dist.CLIENT)
	public static int getRGBDurabilityForDisplay(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return 0;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return MathHelper.hsvToRGB(
				Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack, usesPerTank))) / 3.0F, 1.0F, 1.0F);
		return backtank.getItem()
			.getRGBDurabilityForDisplay(backtank);
	}

	@OnlyIn(Dist.CLIENT)
	public static double getDurabilityForDisplay(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return 0;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return (double) stack.getDamage() / (double) stack.getMaxDamage();
		return backtank.getItem()
			.getDurabilityForDisplay(backtank);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean showDurabilityBar(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return false;
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return false;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return stack.isDamaged();
		return true;
	}

}
