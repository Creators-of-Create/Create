package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class MechanicalCraftingRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

	CRUSHING_WHEEL = create(AllBlocks.CRUSHING_WHEEL::get).returns(2)
		.recipe(b -> b.key('P', ItemTags.PLANKS)
			.key('S', I.stone())
			.key('A', I.andesite())
			.patternLine(" AAA ")
			.patternLine("AAPAA")
			.patternLine("APSPA")
			.patternLine("AAPAA")
			.patternLine(" AAA ")),

		INTEGRATED_CIRCUIT = create(AllItems.INTEGRATED_CIRCUIT::get).returns(1)
			.recipe(b -> b.key('L', AllItems.LAPIS_SHEET.get())
				.key('R', I.redstone())
				.key('Q', AllItems.POLISHED_ROSE_QUARTZ.get())
				.key('C', Tags.Items.NUGGETS_GOLD)
				.patternLine("  L  ")
				.patternLine("RRQRR")
				.patternLine(" CCC ")),

		EXTENDO_GRIP = create(AllItems.EXTENDO_GRIP::get).returns(1)
			.recipe(b -> b.key('L', I.brass())
				.key('R', I.cog())
				.key('H', AllItems.BRASS_HAND.get())
				.key('S', Tags.Items.RODS_WOODEN)
				.patternLine(" L ")
				.patternLine(" R ")
				.patternLine("SSS")
				.patternLine("SSS")
				.patternLine(" H ")),

		FURNACE_ENGINE = create(AllBlocks.FURNACE_ENGINE::get).returns(1)
			.recipe(b -> b.key('P', I.brassSheet())
				.key('B', I.brass())
				.key('I', Ingredient.fromItems(Blocks.PISTON, Blocks.STICKY_PISTON))
				.key('C', I.brassCasing())
				.patternLine("PPB")
				.patternLine("PCI")
				.patternLine("PPB")),

		FLYWHEEL = create(AllBlocks.FLYWHEEL::get).returns(1)
			.recipe(b -> b.key('B', I.brass())
				.key('C', I.brassCasing())
				.patternLine(" BBB")
				.patternLine("CB B")
				.patternLine(" BBB")),

		NIXIE_TUBE = create(AllBlocks.NIXIE_TUBE::get).returns(1)
			.recipe(b -> b.key('E', I.electronTube())
				.key('B', I.brassCasing())
				.patternLine("EBE")),

		MECHANICAL_ARM = create(AllBlocks.MECHANICAL_ARM::get).returns(1)
			.recipe(b -> b.key('L', I.brassSheet())
				.key('R', I.cog())
				.key('I', I.circuit())
				.key('A', I.andesite())
				.key('C', I.brassCasing())
				.patternLine("LLA")
				.patternLine("L  ")
				.patternLine("LL ")
				.patternLine(" I ")
				.patternLine("RCR"))

	;

	public MechanicalCraftingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	GeneratedRecipeBuilder create(Supplier<IItemProvider> result) {
		return new GeneratedRecipeBuilder(result);
	}

	class GeneratedRecipeBuilder {

		private String suffix;
		private Supplier<IItemProvider> result;
		private int amount;

		public GeneratedRecipeBuilder(Supplier<IItemProvider> result) {
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
