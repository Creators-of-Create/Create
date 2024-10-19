package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(Items.HONEY_BOTTLE)
		.output(AllFluids.HONEY.get(), 250)
		.output(Items.GLASS_BOTTLE)),

		BUILDERS_TEA = create("builders_tea", b -> b.require(AllItems.BUILDERS_TEA.get())
			.output(AllFluids.TEA.get(), 250)
			.output(Items.GLASS_BOTTLE)),

		FD_MILK = create(Mods.FD.recipeId("milk_bottle"), b -> b.require(Mods.FD, "milk_bottle")
			.output(ForgeMod.MILK.get(), 250)
			.output(Items.GLASS_BOTTLE)
			.whenModLoaded(Mods.FD.getId())),

		AM_LAVA = create(Mods.AM.recipeId("lava_bottle"), b -> b.require(Mods.AM, "lava_bottle")
				.output(Items.GLASS_BOTTLE)
				.output(Fluids.LAVA, 250)
				.whenModLoaded(Mods.AM.getId())),

		NEO_MILK = create(Mods.NEA.recipeId("milk_bottle"), b -> b.require(Mods.FD, "milk_bottle")
				.output(ForgeMod.MILK.get(), 250)
				.output(Items.GLASS_BOTTLE)
				.whenModLoaded(Mods.NEA.getId())),

		AET_MILK = create(Mods.AET.recipeId("milk_bucket"), b -> b.require(Mods.AET, "skyroot_milk_bucket")
				.output(ForgeMod.MILK.get(), 1000)
				.output(Mods.AET, "skyroot_bucket")
				.whenModLoaded(Mods.AET.getId())),

		AET_WATER = create(Mods.AET.recipeId("water_bucket"), b -> b.require(Mods.AET, "skyroot_water_bucket")
				.output(Fluids.WATER, 1000)
				.output(Mods.AET, "skyroot_bucket")
				.whenModLoaded(Mods.AET.getId())),

		D_AET_POISON_1 = create(Mods.D_AET.recipeId("poison_bucket"), b -> b.require(Mods.D_AET, "poison_bucket")
				.output(Mods.D_AET, "poison_fluid", 1000)
				.output(Items.BUCKET)
				.whenModLoaded(Mods.D_AET.getId())),

		D_AET_POISON_2 = create(Mods.D_AET.recipeId("skyroot_poison_bucket"), b -> b.require(Mods.AET, "skyroot_poison_bucket")
				.output(Mods.D_AET, "poison_fluid", 1000)
				.output(Mods.AET, "skyroot_bucket")
				.whenModLoaded(Mods.D_AET.getId()))

	;

	public EmptyingRecipeGen(PackOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
