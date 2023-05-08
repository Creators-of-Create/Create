package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BacktankUtil {

	public static ItemStack get(LivingEntity entity) {
		for (ItemStack itemStack : entity.getArmorSlots())
			if (AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(itemStack))
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

	public static void consumeAir(LivingEntity entity, ItemStack backtank, float i) {
		CompoundTag tag = backtank.getOrCreateTag();
		int maxAir = maxAir(backtank);
		float air = getAir(backtank);
		float newAir = air - i;
		tag.putFloat("Air", Math.min(newAir, maxAir));
		backtank.setTag(tag);

		if (!(entity instanceof ServerPlayer player))
			return;
		sendWarning(player, air, newAir, maxAir / 10f);
		sendWarning(player, air, newAir, 1);
	}

	private static void sendWarning(ServerPlayer player, float air, float newAir, float threshold) {
		if (newAir > threshold)
			return;
		if (air <= threshold)
			return;

		boolean depleted = threshold == 1;
		MutableComponent component = Lang.translateDirect(depleted ? "backtank.depleted" : "backtank.low");

		AllSoundEvents.DENY.play(player.level, null, player.blockPosition(), 1, 1.25f);
		AllSoundEvents.STEAM.play(player.level, null, player.blockPosition(), .5f, .5f);

		player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
		player.connection.send(new ClientboundSetSubtitleTextPacket(
			Components.literal("\u26A0 ").withStyle(depleted ? ChatFormatting.RED : ChatFormatting.GOLD)
				.append(component.withStyle(ChatFormatting.GRAY))));
		player.connection.send(new ClientboundSetTitleTextPacket(Components.immutableEmpty()));
	}

	public static int maxAir(ItemStack backtank) {
		return maxAir(backtank.getEnchantmentLevel(AllEnchantments.CAPACITY.get()));
	}

	public static int maxAir(int enchantLevel) {
		return AllConfigs.server().curiosities.airInBacktank.get()
			+ AllConfigs.server().curiosities.enchantedBacktankCapacity.get() * enchantLevel;
	}

	public static int maxAirWithoutEnchants() {
		return AllConfigs.server().curiosities.airInBacktank.get();
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
		consumeAir(entity, backtank, cost);
		return true;
	}

	// For Air-using tools

	public static boolean isBarVisible(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return false;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return false;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return stack.isDamaged();
		return true;
	}

	public static int getBarWidth(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 13;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return 13;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return Math.round(13.0F - (float) stack.getDamageValue() / stack.getMaxDamage() * 13.0F);
		return backtank.getItem()
			.getBarWidth(backtank);
	}

	public static int getBarColor(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return 0;
		ItemStack backtank = get(player);
		if (backtank.isEmpty() || !hasAirRemaining(backtank))
			return Mth.hsvToRgb(Math.max(0.0F, 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage()) / 3.0F,
				1.0F, 1.0F);
		return backtank.getItem()
			.getBarColor(backtank);
	}

}
