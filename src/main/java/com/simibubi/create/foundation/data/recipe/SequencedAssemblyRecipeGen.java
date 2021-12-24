package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.fan.SoulSmokingRecipe;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

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
		),

	NETHERRACK = create("netherrack", b->b.require(Items.BLACKSTONE)
			.transitionTo(AllItems.INCOMPLETE_NETHERRACK.get())
			.addOutput(new ItemStack(Blocks.NETHERRACK,2),35)
			.addOutput(new ItemStack(Blocks.BLACKSTONE, 1), 5)
			.addOutput(new ItemStack(Blocks.BASALT, 1), 4)
			.addOutput(new ItemStack(Blocks.DEEPSLATE, 1), 3)
			.addOutput(new ItemStack(Blocks.MAGMA_BLOCK, 1), 2)
			.addOutput(new ItemStack(Blocks.OBSIDIAN, 1), 1)
			.loops(2)
			.addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.CINDER_FLOUR.get()))
			.addStep(FillingRecipe::new, rb -> rb.require(Fluids.LAVA,50))
			.addStep(CuttingRecipe::new, rb -> rb.duration(50))
	)
	;

	public SequencedAssemblyRecipeGen(DataGenerator p_i48262_1_) {
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
