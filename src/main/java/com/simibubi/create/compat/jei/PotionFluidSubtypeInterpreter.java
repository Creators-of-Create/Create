package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.foundation.utility.NBTHelper;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fluids.FluidStack;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {

	@Override
	public String apply(FluidStack ingredient, UidContext context) {
		if (!ingredient.hasTag())
			return IIngredientSubtypeInterpreter.NONE;

		CompoundNBT tag = ingredient.getOrCreateTag();
		Potion potionType = PotionUtils.getPotion(tag);
		String potionTypeString = potionType.getName("");
		String bottleType = NBTHelper.readEnum(tag, "Bottle", BottleType.class)
			.toString();

		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<EffectInstance> effects = PotionUtils.getCustomEffects(tag);

		stringBuilder.append(";")
			.append(bottleType);
		for (EffectInstance effect : potionType.getEffects())
			stringBuilder.append(";")
				.append(effect);
		for (EffectInstance effect : effects)
			stringBuilder.append(";")
				.append(effect);
		return stringBuilder.toString();
	}

}
