package com.simibubi.create.content.contraptions.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionFluid extends ForgeFlowingFluid {

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}
	 
	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack withEffects(int amount, Potion potion, List<EffectInstance> customEffects) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
			.getStillFluid(), amount);
		addPotionToFluidStack(fluidStack, potion);
		appendEffects(fluidStack, customEffects);
		return fluidStack;
	}

	@Override
	public Fluid getStillFluid() {
		return this;
	}

	@Override
	public Fluid getFlowingFluid() {
		return this;
	}

	@Override
	public Item getFilledBucket() {
		return Items.AIR;
	}

	@Override
	protected BlockState getBlockState(FluidState state) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(FluidState p_207193_1_) {
		return false;
	}

	@Override
	public int getLevel(FluidState p_207192_1_) {
		return 0;
	}

	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundNBT tag = stack.getOrCreateTag();
			int color = PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(tag)) | 0xff000000;
			return color;
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
			listnbt.add(effectinstance.write(new CompoundNBT()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return fs;
	}

}
