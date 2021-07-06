package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DivingHelmetItem extends CopperArmorItem {

	public DivingHelmetItem(Properties p_i48534_3_) {
		super(EquipmentSlotType.HEAD, p_i48534_3_);
	}

	@SubscribeEvent
	public static void breatheUnderwater(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World world = entity.world;
		boolean second = world.getGameTime() % 20 == 0;
		boolean drowning = entity.getAir() == 0;

		if (world.isRemote)
			entity.getPersistentData()
				.remove("VisualBacktankAir");

		if (!AllItems.DIVING_HELMET.get()
			.isWornBy(entity))
			return;
		if (!entity.areEyesInFluid(FluidTags.WATER))
			return;
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())
			return;

		ItemStack backtank = BackTankUtil.get(entity);
		if (backtank.isEmpty())
			return;
		if (!BackTankUtil.hasAirRemaining(backtank))
			return;

		if (drowning)
			entity.setAir(10);

		if (world.isRemote)
			entity.getPersistentData()
				.putInt("VisualBacktankAir", (int) BackTankUtil.getAir(backtank));

		if (!second)
			return;

		entity.setAir(Math.min(entity.getMaxAir(), entity.getAir() + 10));
		entity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, 30, 0, true, false, true));
		BackTankUtil.consumeAir(backtank, 1);
	}

}
