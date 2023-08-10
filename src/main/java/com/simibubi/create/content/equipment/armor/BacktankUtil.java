package com.simibubi.create.content.equipment.armor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.backtank_utils.BacktankAirSource;
import com.simibubi.create.content.equipment.armor.backtank_utils.IAirSource;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BacktankUtil {

	private static final List<Function<LivingEntity, List<IAirSource>>> BACKTANK_SUPPLIERS = new ArrayList<>();

	static {
		addBacktankSupplier(entity -> {
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack itemStack : entity.getArmorSlots())
				if (AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(itemStack))
					stacks.add(itemStack);

			return stacks;
		});
	}

	/**
	 * Get all Air Sources from the entity that have some air.
	 * Will return empty list if all Air Sources (i.e. Backtanks) are empty.
	 * @param entity player or other entity to check
	 * @return list of Air Sources
	 */
	public static List<IAirSource> getAllWithAir(LivingEntity entity) {
		List<IAirSource> all = new ArrayList<>();

		for (Function<LivingEntity, List<IAirSource>> supplier : BACKTANK_SUPPLIERS) {
			List<IAirSource> result = supplier.apply(entity);

			for (IAirSource stack : result)
				if (stack.hasAirRemaining())
					all.add(stack);
		}

		// Sort with ascending order (we want to prioritize the most empty so things actually run out)
		all.sort((a, b) -> Float.compare(a.getAir(), b.getAir()));

		return all;
	}

	/**
	 * Try to use air from any Air Source available.
	 * @param entity player or other entity to check
	 * @param usesPerTank how many uses does the tool have per non-enchanted Copper Backtank (used to calculate cost)
	 * @return if air was consumed
	 */

	public static boolean canAbsorbDamage(LivingEntity entity, int usesPerTank) {
		if (usesPerTank == 0)
			return true;
		if (entity instanceof Player && ((Player) entity).isCreative())
			return true;
		List<IAirSource> backtanks = getAllWithAir(entity);
		if (backtanks.isEmpty())
			return false;
		float cost = ((float) BacktankAirSource.maxAirWithoutEnchants()) / usesPerTank;
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
		List<IAirSource> backtanks = getAllWithAir(player);
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

		List<IAirSource> backtanks = getAllWithAir(player);

		if (backtanks.isEmpty())
			return Math.round(13.0F - (float) stack.getDamageValue() / stack.getMaxDamage() * 13.0F);

		if (backtanks.size() == 1)
			return backtanks.get(0).getBarWidth();

		// If there is more than one backtank, average the bar widths.
		int sumBarWidth = backtanks.stream()
			.map(IAirSource::getBarWidth)
			.reduce(0, Integer::sum);

		return Math.round((float) sumBarWidth / backtanks.size());
	}

	public static int getBarColor(ItemStack stack, int usesPerTank) {
		if (usesPerTank == 0)
			return 0;
		Player player = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
		if (player == null)
			return 0;
		List<IAirSource> backtanks = getAllWithAir(player);

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
				supplier.apply(entity).stream().map(BacktankAirSource::new).collect(Collectors.toList())
		);
	}

	/**
	 * Use this method to add entry points to custom, non-backtank air sources.
	 */
	public static void addBacktankWrapperSupplier(Function<LivingEntity, List<IAirSource>> supplier) {
		BACKTANK_SUPPLIERS.add(supplier);
	}

}
