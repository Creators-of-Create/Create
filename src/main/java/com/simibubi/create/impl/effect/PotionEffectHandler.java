package com.simibubi.create.impl.effect;

import java.util.List;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.fluids.FluidStack;

public class PotionEffectHandler implements OpenPipeEffectHandler {
	@Override
	public void apply(Level level, AABB area, FluidStack fluid) {
		PotionContents contents = getContents(fluid);
		if (contents == PotionContents.EMPTY)
			return;

		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions);
		for (LivingEntity entity : entities) {
			contents.forEachEffect(effectInstance -> {
				MobEffect effect = effectInstance.getEffect().value();
				if (effect.isInstantenous()) {
					effect.applyInstantenousEffect(null, null, entity, effectInstance.getAmplifier(), 0.5D);
				} else {
					entity.addEffect(new MobEffectInstance(effectInstance));
				}
			});
		}
	}

	private static PotionContents getContents(FluidStack fluid) {
		FluidStack copy = fluid.copy();
		copy.setAmount(250);
		ItemStack bottle = PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), copy);
		return bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}
}
