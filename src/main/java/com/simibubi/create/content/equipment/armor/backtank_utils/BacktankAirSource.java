package com.simibubi.create.content.equipment.armor.backtank_utils;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class BacktankAirSource implements IAirSource {
	private final ItemStack backtankStack;

	public BacktankAirSource(ItemStack backtankStack) {
		this.backtankStack = backtankStack;
	}

	@Override
	public float getAir() {
		return getAir(backtankStack);
	}

//	public int maxAir() {
//		return maxAir(backtankStack);
//	}


	@Override
	public void consumeAir(LivingEntity entity, float i) {
		consumeAir(entity, backtankStack, i);
	}

	@Override
	public boolean hasAirRemaining() {
		return hasAirRemaining(backtankStack);
	}

	@Override
	public int getBarWidth() {
		return backtankStack.getBarWidth();
	}

	@Override
	public int getBarColor() {
		return backtankStack.getBarColor();
	}

	@Override
	public boolean isFireResistant() {
		return backtankStack.getItem().isFireResistant();
	}

	@Override
	public ItemStack getDisplayedBacktank() {
		return backtankStack;
	}


	// Static methods moved from BacktankUtil

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
		float newAir = Math.max(air - i, 0);
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
		return maxAir(EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), backtank));
	}

	public static int maxAir(int enchantLevel) {
		return AllConfigs.server().equipment.airInBacktank.get()
				+ AllConfigs.server().equipment.enchantedBacktankCapacity.get() * enchantLevel;
	}

	public static int maxAirWithoutEnchants() {
		return AllConfigs.server().equipment.airInBacktank.get();
	}

}
