package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.lib.util.EntityHelper;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DivingHelmetItem extends CopperArmorItem {

	public DivingHelmetItem(Properties p_i48534_3_) {
		super(EquipmentSlot.HEAD, p_i48534_3_);
	}

	public static void breatheUnderwater(LivingEntity entity) {
//		LivingEntity entity = event.getEntityLiving();
		Level world = entity.level;
		boolean second = world.getGameTime() % 20 == 0;
		boolean drowning = entity.getAirSupply() == 0;

		if (world.isClientSide)
			EntityHelper.getExtraCustomData(entity)
				.remove("VisualBacktankAir");

		if (!AllItems.DIVING_HELMET.get()
			.isWornBy(entity))
			return;
		if (!entity.isEyeInFluid(FluidTags.WATER))
			return;
		if (entity instanceof Player && ((Player) entity).isCreative())
			return;

		ItemStack backtank = BackTankUtil.get(entity);
		if (backtank.isEmpty())
			return;
		if (!BackTankUtil.hasAirRemaining(backtank))
			return;

		if (drowning)
			entity.setAirSupply(10);

		if (world.isClientSide)
			EntityHelper.getExtraCustomData(entity)
				.putInt("VisualBacktankAir", (int) BackTankUtil.getAir(backtank));

		if (!second)
			return;

		entity.setAirSupply(Math.min(entity.getMaxAirSupply(), entity.getAirSupply() + 10));
		entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false, true));
		BackTankUtil.consumeAir(backtank, 1);
	}

}
