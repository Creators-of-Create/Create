package com.simibubi.create.content.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluid extends VirtualFluid {

	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack of(int amount, Potion potion) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
			.getSource(), amount);
		addPotionToFluidStack(fluidStack, potion);
		return fluidStack;
	}

	public static FluidStack withEffects(int amount, Potion potion, List<MobEffectInstance> customEffects) {
		FluidStack fluidStack = of(amount, potion);
		appendEffects(fluidStack, customEffects);
		return fluidStack;
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = CatnipServices.REGISTRIES.getKeyOrThrow(potion);
		if (potion == Potions.EMPTY) {
			fs.removeChildTag("Potion");
			return fs;
		}
		fs.getOrCreateTag()
			.putString("Potion", resourcelocation.toString());
		return fs;
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundTag compoundnbt = fs.getOrCreateTag();
		ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (MobEffectInstance effectinstance : customEffects)
			listnbt.add(effectinstance.save(new CompoundTag()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return fs;
	}

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}

	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
			return color;
		}

		@Override
		public Component getDisplayName(FluidStack stack) {
			return Components.translatable(getTranslationKey(stack));
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			ItemLike itemFromBottleType =
				PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
			return PotionUtils.getPotion(tag)
				.getName(itemFromBottleType.asItem()
					.getDescriptionId() + ".effect.");
		}

	}

}
