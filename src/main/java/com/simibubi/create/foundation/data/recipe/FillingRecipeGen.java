package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;

import me.alphamode.forgetags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(AllFluidTags.HONEY.tag, FluidConstants.BOTTLE)
		.require(Items.GLASS_BOTTLE)
		.output(Items.HONEY_BOTTLE)),

		BUILDERS_TEA = create("builders_tea", b -> b.require(AllFluids.TEA.get(), FluidConstants.BOTTLE)
			.require(Items.GLASS_BOTTLE)
			.output(AllItems.BUILDERS_TEA.get())),

		BLAZE_CAKE = create("blaze_cake", b -> b.require(Fluids.LAVA, FluidConstants.BOTTLE)
			.require(AllItems.BLAZE_CAKE_BASE.get())
			.output(AllItems.BLAZE_CAKE.get())),

		HONEYED_APPLE = create("honeyed_apple", b -> b.require(AllFluidTags.HONEY.tag, FluidConstants.BOTTLE)
			.require(Items.APPLE)
			.output(AllItems.HONEYED_APPLE.get())),

		SWEET_ROLL = create("sweet_roll", b -> b.require(Tags.Fluids.MILK, FluidConstants.BOTTLE)
			.require(Items.BREAD)
			.output(AllItems.SWEET_ROLL.get())),

		CHOCOLATE_BERRIES = create("chocolate_glazed_berries", b -> b.require(AllFluids.CHOCOLATE.get(), FluidConstants.BOTTLE)
			.require(Items.SWEET_BERRIES)
			.output(AllItems.CHOCOLATE_BERRIES.get())),

		GRASS_BLOCK = create("grass_block", b -> b.require(Fluids.WATER, FluidConstants.BUCKET / 2)
			.require(Items.DIRT)
			.output(Items.GRASS_BLOCK)),

		GUNPOWDER = create("gunpowder", b -> b.require(PotionFluidHandler.potionIngredient(Potions.HARMING, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GUNPOWDER)),

		REDSTONE = create("redstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.STRENGTH, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.REDSTONE)),

		GLOWSTONE = create("glowstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.NIGHT_VISION, FluidConstants.BUCKET / 40))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GLOWSTONE_DUST))

	;

	public FillingRecipeGen(FabricDataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
