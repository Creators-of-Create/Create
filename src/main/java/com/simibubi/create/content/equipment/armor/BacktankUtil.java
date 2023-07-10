package com.simibubi.create.content.equipment.armor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BacktankUtil {

	private static final List<Function<LivingEntity, List<BacktankWrapper>>> BACKTANK_SUPPLIERS = new ArrayList<>();

	static {
		addBacktankSupplier(entity -> {
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack itemStack : entity.getArmorSlots())
				if (AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(itemStack))
					stacks.add(itemStack);

			return stacks;
		});
	}

	public static List<BacktankWrapper> getAllWithAir(LivingEntity entity) {
		List<BacktankWrapper> all = new ArrayList<>();

		for (Function<LivingEntity, List<BacktankWrapper>> supplier : BACKTANK_SUPPLIERS) {
			List<BacktankWrapper> result = supplier.apply(entity);

			for (BacktankWrapper stack : result)
				if (stack.hasAirRemaining())
					all.add(stack);
		}

		// Sort with ascending order (we want to prioritize the most empty so things actually run out)
		all.sort((a, b) -> Float.compare(a.getAir(), b.getAir()));

		return all;
	}

	// BackTank-specific functions - could be moved to DefaultBacktankWrapper

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

	// BackTank-agnostic functions

	public static boolean canAbsorbDamage(LivingEntity entity, int usesPerTank) {
		if (usesPerTank == 0)
			return true;
		if (entity instanceof Player && ((Player) entity).isCreative())
			return true;
		List<BacktankWrapper> backtanks = getAllWithAir(entity);
		if (backtanks.isEmpty())
			return false;
		float cost = ((float) maxAirWithoutEnchants()) / usesPerTank;
		backtanks.get(0).consumeAir(entity, cost);
		return true;
	}

	// For Air-using tools

	public static boolean isBarVisible(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return false;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return false;
		List<BacktankWrapper> backtanks = getAllWithAir(player);
		if (backtanks.isEmpty())
			return stack.isDamaged();
		return true;
	}

	public static int getBarWidth(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 13;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return 13;

		List<BacktankWrapper> backtanks = getAllWithAir(player);

		if (backtanks.isEmpty())
			return Math.round(13.0F - (float) stack.getDamageValue() / stack.getMaxDamage() * 13.0F);

		if (backtanks.size() == 1)
			return backtanks.get(0).getBarWidth();

		// If there is more than one backtank, average the bar widths.
		int sumBarWidth = backtanks.stream()
			.map(BacktankWrapper::getBarWidth)
			.reduce(0, Integer::sum);

		return Math.round((float) sumBarWidth / backtanks.size());
	}

	public static int getBarColor(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return 0;
		List<BacktankWrapper> backtanks = getAllWithAir(player);

		// Fallback colour
		if (backtanks.isEmpty())
			return Mth.hsvToRgb(Math.max(0.0F, 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage()) / 3.0F,
				1.0F, 1.0F);

		// Just return the "first" backtank for the bar color since that's the one we are consuming from
		return backtanks.get(0).getBarColor();
	}

	/**
	 * Use this method to add custom entry points to the backtank item stack supplier, e.g. getting them from custom
	 * slots or items.
	 */
	public static void addBacktankSupplier(Function<LivingEntity, List<ItemStack>> supplier) {
		BACKTANK_SUPPLIERS.add(entity ->
				supplier.apply(entity).stream().map(DefaultBacktankWrapper::new).collect(Collectors.toList())
		);
	}
	public static void addBacktankWrapperSupplier(Function<LivingEntity, List<BacktankWrapper>> supplier) {
		BACKTANK_SUPPLIERS.add(supplier);
	}

	public interface BacktankWrapper {
		float getAir();
		int maxAir();
		void consumeAir(LivingEntity entity, float i);
		boolean hasAirRemaining();

		int getBarWidth();

		int getBarColor();

		boolean isFireResistant();

		ItemStack getDisplayedBacktank();
	}

	public static class DefaultBacktankWrapper implements BacktankWrapper {
		private final ItemStack backtankStack;

		public DefaultBacktankWrapper(ItemStack backtankStack) {
			this.backtankStack = backtankStack;
		}

		@Override
		public float getAir() {
			return BacktankUtil.getAir(backtankStack);
		}

		public int maxAir() {
			return BacktankUtil.maxAir(backtankStack);
		}


		@Override
		public void consumeAir(LivingEntity entity, float i) {
			BacktankUtil.consumeAir(entity, backtankStack, i);
		}

		@Override
		public boolean hasAirRemaining() {
			return BacktankUtil.hasAirRemaining(backtankStack);
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
	}
}
