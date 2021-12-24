package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipeBuilder;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class SequencedAssemblyRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

//	TEST = create("test", b -> b.require(I.goldSheet())
//		.transitionTo(AllItems.INCOMPLETE_CLOCKWORK_COMPONENT.get())
//		.addOutput(AllItems.CLOCKWORK_COMPONENT.get(), 10)
//		.addOutput(AllItems.GOLDEN_SHEET.get(), 5)
//		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
//		.addOutput(AllBlocks.COGWHEEL.get(), 1)
//		.loops(5)
//		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
//		.addStep(PressingRecipe::new, rb -> rb)
//		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.LAVA, 500))
//		.addStep(CuttingRecipe::new, rb -> rb.averageProcessingDuration())
//		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.WATER, 100))
//		)

	PRECISION_MECHANISM = create("precision_mechanism", b -> b.require(I.goldSheet())
		.transitionTo(AllItems.INCOMPLETE_PRECISION_MECHANISM.get())
		.addOutput(AllItems.PRECISION_MECHANISM.get(), 120)
		.addOutput(AllItems.GOLDEN_SHEET.get(), 8)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 8)
		.addOutput(AllBlocks.COGWHEEL.get(), 5)
		.addOutput(AllBlocks.SHAFT.get(), 2)
		.addOutput(AllItems.CRUSHED_GOLD.get(), 2)
		.addOutput(Items.GOLD_NUGGET, 2)
		.addOutput(Items.IRON_INGOT, 1)
		.addOutput(Items.CLOCK, 1)
		.loops(5)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.largeCog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.ironNugget()))
		),

	COGWHEEL = create("cogwheel", b -> b.require(I.andesite())
		.transitionTo(AllItems.INCOMPLETE_COGWHEEL.get())
		.addOutput(new ItemStack(AllBlocks.COGWHEEL.get(), 12), 32)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
		.addOutput(Blocks.ANDESITE, 1)
		.addOutput(AllBlocks.LARGE_COGWHEEL.get(), 1)
		.addOutput(Items.STICK, 1)
		.addOutput(Items.IRON_NUGGET, 1)
		.loops(4)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(ItemTags.WOODEN_BUTTONS))
		.addStep(CuttingRecipe::new, rb -> rb.duration(50))
		),

	LARGE_COGWHEEL = create("large_cogwheel", b -> b.require(I.andesite())
		.transitionTo(AllItems.INCOMPLETE_LARGE_COGWHEEL.get())
		.addOutput(new ItemStack(AllBlocks.LARGE_COGWHEEL.get(), 6), 32)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
		.addOutput(Blocks.ANDESITE, 1)
		.addOutput(AllBlocks.COGWHEEL.get(), 1)
		.addOutput(Items.STICK, 1)
		.addOutput(Items.IRON_NUGGET, 1)
		.loops(3)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.planks()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(ItemTags.WOODEN_BUTTONS))
		.addStep(CuttingRecipe::new, rb -> rb.duration(50))
		)
	;

	public SequencedAssemblyRecipeGen(FabricDataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	protected GeneratedRecipe create(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform) {
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new SequencedAssemblyRecipeBuilder(Create.asResource(name)))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	@Override
	public String getName() {
		return "Create's Sequenced Assembly Recipes";
	}

}
