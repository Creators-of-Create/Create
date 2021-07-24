package com.simibubi.create.content.contraptions.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionFluid extends VirtualFluid {

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}

	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack withEffects(int amount, Potion potion, List<EffectInstance> customEffects) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
			.getSource(), amount);
		addPotionToFluidStack(fluidStack, potion);
		appendEffects(fluidStack, customEffects);
		return fluidStack;
	}

	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundNBT tag = stack.getOrCreateTag();
			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
			return color;
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			CompoundNBT tag = stack.getOrCreateTag();
			IItemProvider itemFromBottleType =
				PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
			return PotionUtils.getPotion(tag)
				.getName(itemFromBottleType.asItem()
					.getDescriptionId() + ".effect.");
		}

	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = ForgeRegistries.POTION_TYPES.getKey(potion);
		if (potion == Potions.EMPTY) {
			fs.removeChildTag("Potion");
			return fs;
		}
		fs.getOrCreateTag()
			.putString("Potion", resourcelocation.toString());
		return fs;
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<EffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundNBT compoundnbt = fs.getOrCreateTag();
		ListNBT listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (EffectInstance effectinstance : customEffects)
			listnbt.add(effectinstance.save(new CompoundNBT()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return fs;
	}

}
