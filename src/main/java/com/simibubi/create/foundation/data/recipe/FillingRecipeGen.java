package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;

import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(AllTags.forgeFluidTag("honey"), 250)
		.require(Items.GLASS_BOTTLE)
		.output(Items.HONEY_BOTTLE)),

		BUILDERS_TEA = create("builders_tea", b -> b.require(AllFluids.TEA.get(), 250)
			.require(Items.GLASS_BOTTLE)
			.output(AllItems.BUILDERS_TEA.get())),

		BLAZE_CAKE = create("blaze_cake", b -> b.require(Fluids.LAVA, 250)
			.require(AllItems.BLAZE_CAKE_BASE.get())
			.output(AllItems.BLAZE_CAKE.get())),
		
		HONEYED_APPLE = create("honeyed_apple", b -> b.require(AllTags.forgeFluidTag("honey"), 250)
			.require(Items.APPLE)
			.output(AllItems.HONEYED_APPLE.get())),
		
		SWEET_ROLL = create("sweet_roll", b -> b.require(AllTags.forgeFluidTag("milk"), 250)
			.require(Items.BREAD)
			.output(AllItems.SWEET_ROLL.get())),
		
		CHOCOLATE_BERRIES = create("chocolate_glazed_berries", b -> b.require(AllFluids.CHOCOLATE.get(), 250)
			.require(Items.SWEET_BERRIES)
			.output(AllItems.CHOCOLATE_BERRIES.get())),

		GRASS_BLOCK = create("grass_block", b -> b.require(Fluids.WATER, 500)
			.require(Items.DIRT)
			.output(Items.GRASS_BLOCK)),

		GUNPOWDER = create("gunpowder", b -> b.require(PotionFluidHandler.potionIngredient(Potions.HARMING, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GUNPOWDER)),

		REDSTONE = create("redstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.STRENGTH, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.REDSTONE)),

		GLOWSTONE = create("glowstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.NIGHT_VISION, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(Items.GLOWSTONE_DUST)),

		MILK_BUCKET = create("milk_bucket", b -> b.require(AllTags.forgeFluidTag("milk"), 1000)
			.require(Items.BUCKET)
			.output(Items.MILK_BUCKET))

	;

	public FillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
