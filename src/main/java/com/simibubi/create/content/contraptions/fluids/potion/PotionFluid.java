package com.simibubi.create.content.contraptions.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.lib.transfer.fluid.FluidAttributes;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public class PotionFluid extends VirtualFluid {

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}

	@SuppressWarnings("UnstableApiUsage")
	public PotionFluid(Properties properties) {
		super(properties);
		FluidVariantRendering.register(this, new FluidVariantRenderHandler() {
			@Override
			public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
				return PotionUtils.getColor(PotionUtils.getAllEffects(fluidVariant.getNbt())) | 0xff000000;
			}
		});
	}

	public static FluidStack withEffects(long amount, Potion potion, List<MobEffectInstance> customEffects) {
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
			CompoundTag tag = stack.getOrCreateTag();
			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
			return color;
		}

		@Override
		public Component getDisplayName(FluidStack stack) {
			return new TranslatableComponent(getTranslationKey(stack));
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

	public static FluidStack  addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = Registry.POTION.getKey(potion);
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

}
