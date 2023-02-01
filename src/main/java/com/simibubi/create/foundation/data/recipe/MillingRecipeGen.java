package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

public class MillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	GRANITE = create(() -> Blocks.GRANITE, b -> b.duration(200)
		.output(Blocks.RED_SAND)),

		WOOL = create("wool", b -> b.duration(100)
			.require(ItemTags.WOOL)
			.output(Items.STRING)),

		CLAY = create(() -> Blocks.CLAY, b -> b.duration(50)
			.output(Items.CLAY_BALL, 3)
			.output(.5f, Items.CLAY_BALL)),

		CALCITE = create(() -> Items.CALCITE, b -> b.duration(250)
			.output(.75f, Items.BONE_MEAL, 1)),
		DRIPSTONE = create(() -> Items.DRIPSTONE_BLOCK, b -> b.duration(250)
			.output(Items.CLAY_BALL, 1)),

		TERRACOTTA = create(() -> Blocks.TERRACOTTA, b -> b.duration(200)
			.output(Blocks.RED_SAND)),
		ANDESITE = create(() -> Blocks.ANDESITE, b -> b.duration(200)
			.output(Blocks.COBBLESTONE)),
		COBBLESTONE = create(() -> Blocks.COBBLESTONE, b -> b.duration(250)
			.output(Blocks.GRAVEL)),
		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.duration(250)
			.output(Items.FLINT)),
		SANDSTONE = create(() -> Blocks.SANDSTONE, b -> b.duration(150)
			.output(Blocks.SAND)),

		WHEAT = create(() -> Items.WHEAT, b -> b.duration(150)
			.output(AllItems.WHEAT_FLOUR.get())
			.output(.25f, AllItems.WHEAT_FLOUR.get(), 2)
			.output(.25f, Items.WHEAT_SEEDS)),

		BONE = create(() -> Items.BONE, b -> b.duration(100)
			.output(Items.BONE_MEAL, 3)
			.output(.25f, Items.WHITE_DYE, 1)
			.output(.25f, Items.BONE_MEAL, 3)),

		CACTUS = create(() -> Blocks.CACTUS, b -> b.duration(50)
			.output(Items.GREEN_DYE, 2)
			.output(.1f, Items.GREEN_DYE, 1)
			.whenModMissing("quark")),

		SEA_PICKLE = create(() -> Blocks.SEA_PICKLE, b -> b.duration(50)
			.output(Items.LIME_DYE, 2)
			.output(.1f, Items.GREEN_DYE)),

		BONE_MEAL = create(() -> Items.BONE_MEAL, b -> b.duration(70)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIGHT_GRAY_DYE, 1)),

		COCOA_BEANS = create(() -> Items.COCOA_BEANS, b -> b.duration(70)
			.output(Items.BROWN_DYE, 2)
			.output(.1f, Items.BROWN_DYE, 1)),

		SADDLE = create(() -> Items.SADDLE, b -> b.duration(200)
			.output(Items.LEATHER, 2)
			.output(.5f, Items.LEATHER, 2)),

		SUGAR_CANE = create(() -> Items.SUGAR_CANE, b -> b.duration(50)
			.output(Items.SUGAR, 2)
			.output(.1f, Items.SUGAR)),

		BEETROOT = create(() -> Items.BEETROOT, b -> b.duration(70)
			.output(Items.RED_DYE, 2)
			.output(.1f, Items.BEETROOT_SEEDS)),

		INK_SAC = create(() -> Items.INK_SAC, b -> b.duration(100)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.GRAY_DYE)),

		CHARCOAL = create(() -> Items.CHARCOAL, b -> b.duration(100)
			.output(Items.BLACK_DYE, 1)
			.output(.1f, Items.GRAY_DYE, 2)),

		COAL = create(() -> Items.COAL, b -> b.duration(100)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.GRAY_DYE, 1)),

		LAPIS_LAZULI = create(() -> Items.LAPIS_LAZULI, b -> b.duration(100)
			.output(Items.BLUE_DYE, 2)
			.output(.1f, Items.BLUE_DYE)),

		AZURE_BLUET = create(() -> Blocks.AZURE_BLUET, b -> b.duration(50)
			.output(Items.LIGHT_GRAY_DYE, 2)
			.output(.1f, Items.WHITE_DYE, 2)),

		BLUE_ORCHID = create(() -> Blocks.BLUE_ORCHID, b -> b.duration(50)
			.output(Items.LIGHT_BLUE_DYE, 2)
			.output(.05f, Items.LIGHT_GRAY_DYE, 1)),

		FERN = create(() -> Blocks.FERN, b -> b.duration(50)
			.output(Items.GREEN_DYE)
			.output(.1f, Items.WHEAT_SEEDS)),

		LARGE_FERN = create(() -> Blocks.LARGE_FERN, b -> b.duration(50)
			.output(Items.GREEN_DYE, 2)
			.output(.5f, Items.GREEN_DYE)
			.output(.1f, Items.WHEAT_SEEDS)),

		LILAC = create(() -> Blocks.LILAC, b -> b.duration(100)
			.output(Items.MAGENTA_DYE, 3)
			.output(.25f, Items.MAGENTA_DYE)
			.output(.25f, Items.PURPLE_DYE)),

		PEONY = create(() -> Blocks.PEONY, b -> b.duration(100)
			.output(Items.PINK_DYE, 3)
			.output(.25f, Items.MAGENTA_DYE)
			.output(.25f, Items.PINK_DYE)),

		ALLIUM = create(() -> Blocks.ALLIUM, b -> b.duration(50)
			.output(Items.MAGENTA_DYE, 2)
			.output(.1f, Items.PURPLE_DYE, 2)
			.output(.1f, Items.PINK_DYE)),

		LILY_OF_THE_VALLEY = create(() -> Blocks.LILY_OF_THE_VALLEY, b -> b.duration(50)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIME_DYE)
			.output(.1f, Items.WHITE_DYE)),

		ROSE_BUSH = create(() -> Blocks.ROSE_BUSH, b -> b.duration(50)
			.output(Items.RED_DYE, 3)
			.output(.05f, Items.GREEN_DYE, 2)
			.output(.25f, Items.RED_DYE, 2)),

		SUNFLOWER = create(() -> Blocks.SUNFLOWER, b -> b.duration(100)
			.output(Items.YELLOW_DYE, 3)
			.output(.25f, Items.ORANGE_DYE)
			.output(.25f, Items.YELLOW_DYE)),

		OXEYE_DAISY = create(() -> Blocks.OXEYE_DAISY, b -> b.duration(50)
			.output(Items.LIGHT_GRAY_DYE, 2)
			.output(.2f, Items.WHITE_DYE)
			.output(.05f, Items.YELLOW_DYE)),

		POPPY = create(() -> Blocks.POPPY, b -> b.duration(50)
			.output(Items.RED_DYE, 2)
			.output(.05f, Items.GREEN_DYE)),

		DANDELION = create(() -> Blocks.DANDELION, b -> b.duration(50)
			.output(Items.YELLOW_DYE, 2)
			.output(.05f, Items.YELLOW_DYE)),

		CORNFLOWER = create(() -> Blocks.CORNFLOWER, b -> b.duration(50)
			.output(Items.BLUE_DYE, 2)),

		WITHER_ROSE = create(() -> Blocks.WITHER_ROSE, b -> b.duration(50)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.BLACK_DYE)),

		ORANGE_TULIP = create(() -> Blocks.ORANGE_TULIP, b -> b.duration(50)
			.output(Items.ORANGE_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		RED_TULIP = create(() -> Blocks.RED_TULIP, b -> b.duration(50)
			.output(Items.RED_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		WHITE_TULIP = create(() -> Blocks.WHITE_TULIP, b -> b.duration(50)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		PINK_TULIP = create(() -> Blocks.PINK_TULIP, b -> b.duration(50)
			.output(Items.PINK_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		TALL_GRASS = create(() -> Blocks.TALL_GRASS, b -> b.duration(100)
			.output(.5f, Items.WHEAT_SEEDS)),
		GRASS = create(() -> Blocks.GRASS, b -> b.duration(50)
			.output(.25f, Items.WHEAT_SEEDS))

	;

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends Item> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get()));
	}

	public MillingRecipeGen(PackOutput output) {
		super(output);
	}
	
	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
