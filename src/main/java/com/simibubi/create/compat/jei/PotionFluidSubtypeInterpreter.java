package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.fluids.FluidStack;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {

	@Override
	public String apply(FluidStack ingredient, UidContext context) {
		if (!ingredient.hasTag())
			return IIngredientSubtypeInterpreter.NONE;

		CompoundTag tag = ingredient.getOrCreateTag();
		Potion potionType = PotionUtils.getPotion(tag);
		String potionTypeString = potionType.getName("");
		String bottleType = NBTHelper.readEnum(tag, "Bottle", BottleType.class)
			.toString();

		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<MobEffectInstance> effects = PotionUtils.getCustomEffects(tag);

		stringBuilder.append(";")
			.append(bottleType);
		for (MobEffectInstance effect : potionType.getEffects())
			stringBuilder.append(";")
				.append(effect);
		for (MobEffectInstance effect : effects)
			stringBuilder.append(";")
				.append(effect);
		return stringBuilder.toString();
	}

}
