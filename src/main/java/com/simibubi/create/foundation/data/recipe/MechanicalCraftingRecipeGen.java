package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

public class MechanicalCraftingRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

	CRUSHING_WHEEL = create(AllBlocks.CRUSHING_WHEEL::get).returns(2)
		.recipe(b -> b.key('P', Ingredient.of(ItemTags.PLANKS))
			.key('S', Ingredient.of(I.stone()))
			.key('A', I.andesite())
			.patternLine(" AAA ")
			.patternLine("AAPAA")
			.patternLine("APSPA")
			.patternLine("AAPAA")
			.patternLine(" AAA ")
			.disallowMirrored()),

		EXTENDO_GRIP = create(AllItems.EXTENDO_GRIP::get).returns(1)
			.recipe(b -> b.key('L', Ingredient.of(I.brass()))
				.key('R', I.precisionMechanism())
				.key('H', AllItems.BRASS_HAND.get())
				.key('S', Ingredient.of(Tags.Items.RODS_WOODEN))
				.patternLine(" L ")
				.patternLine(" R ")
				.patternLine("SSS")
				.patternLine("SSS")
				.patternLine(" H ")
				.disallowMirrored()),

		POTATO_CANNON = create(AllItems.POTATO_CANNON::get).returns(1)
			.recipe(b -> b.key('L', I.andesite())
				.key('R', I.precisionMechanism())
				.key('S', AllBlocks.FLUID_PIPE.get())
				.key('C', Ingredient.of(I.copper()))
				.patternLine("LRSSS")
				.patternLine("CC   ")),

		FURNACE_ENGINE = create(AllBlocks.FURNACE_ENGINE::get).returns(1)
			.recipe(b -> b.key('P', Ingredient.of(I.brassSheet()))
				.key('B', Ingredient.of(I.brass()))
				.key('I', Ingredient.of(Blocks.PISTON, Blocks.STICKY_PISTON))
				.key('C', I.brassCasing())
				.patternLine("PPB")
				.patternLine("PCI")
				.patternLine("PPB")),

		FLYWHEEL = create(AllBlocks.FLYWHEEL::get).returns(1)
			.recipe(b -> b.key('B', Ingredient.of(I.brass()))
				.key('C', I.brassCasing())
				.patternLine(" BBB")
				.patternLine("CB B")
				.patternLine(" BBB"))

	;

	public MechanicalCraftingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
		return new GeneratedRecipeBuilder(result);
	}

	class GeneratedRecipeBuilder {

		private String suffix;
		private Supplier<ItemLike> result;
		private int amount;

		public GeneratedRecipeBuilder(Supplier<ItemLike> result) {
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe recipe(UnaryOperator<MechanicalCraftingRecipeBuilder> builder) {
			return register(consumer -> {
				MechanicalCraftingRecipeBuilder b =
					builder.apply(MechanicalCraftingRecipeBuilder.shapedRecipe(result.get(), amount));
				ResourceLocation location = Create.asResource("mechanical_crafting/" + result.get()
					.asItem()
					.getRegistryName()
					.getPath() + suffix);
				b.build(consumer, location);
			});
		}
	}

	@Override
	public String getName() {
		return "Create's Mechanical Crafting Recipes";
	}

}
