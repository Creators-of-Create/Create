package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.Mods.EID;
import static com.simibubi.create.foundation.data.recipe.Mods.IE;
import static com.simibubi.create.foundation.data.recipe.Mods.INF;
import static com.simibubi.create.foundation.data.recipe.Mods.MEK;
import static com.simibubi.create.foundation.data.recipe.Mods.MW;
import static com.simibubi.create.foundation.data.recipe.Mods.SM;
import static com.simibubi.create.foundation.data.recipe.Mods.TH;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class WashingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	WOOL = create("wool", b -> b.require(ItemTags.WOOL)
		.output(Items.WHITE_WOOL)),

		STAINED_GLASS = create("stained_glass", b -> b.require(Tags.Items.STAINED_GLASS)
			.output(Items.GLASS)),
		STAINED_GLASS_PANE = create("stained_glass_pane", b -> b.require(Tags.Items.STAINED_GLASS_PANES)
			.output(Items.GLASS_PANE)),

		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.output(.25f, Items.FLINT)
			.output(.125f, Items.IRON_NUGGET)),
		SOUL_SAND = create(() -> Blocks.SOUL_SAND, b -> b.output(.125f, Items.QUARTZ, 4)
			.output(.02f, Items.GOLD_NUGGET)),
		RED_SAND = create(() -> Blocks.RED_SAND, b -> b.output(.125f, Items.GOLD_NUGGET, 3)
			.output(.05f, Items.DEAD_BUSH)),
		SAND = create(() -> Blocks.SAND, b -> b.output(.25f, Items.CLAY_BALL)),

		CRUSHED_COPPER = crushedOre(AllItems.CRUSHED_COPPER, AllItems.COPPER_NUGGET::get),
		CRUSHED_ZINC = crushedOre(AllItems.CRUSHED_ZINC, AllItems.ZINC_NUGGET::get),
		CRUSHED_BRASS = crushedOre(AllItems.CRUSHED_BRASS, AllItems.BRASS_NUGGET::get),
		CRUSHED_GOLD = crushedOre(AllItems.CRUSHED_GOLD, () -> Items.GOLD_NUGGET),
		CRUSHED_IRON = crushedOre(AllItems.CRUSHED_IRON, () -> Items.IRON_NUGGET),

		CRUSHED_OSMIUM = moddedCrushedOre(AllItems.CRUSHED_OSMIUM, "osmium", MEK),
		CRUSHED_PLATINUM = moddedCrushedOre(AllItems.CRUSHED_PLATINUM, "platinum", SM),
		CRUSHED_SILVER = moddedCrushedOre(AllItems.CRUSHED_SILVER, "silver", TH, MW, IE, SM, INF),
		CRUSHED_TIN = moddedCrushedOre(AllItems.CRUSHED_TIN, "tin", TH, MEK, MW, SM),
		CRUSHED_LEAD = moddedCrushedOre(AllItems.CRUSHED_LEAD, "lead", MEK, TH, MW, IE, SM, EID),
		CRUSHED_QUICKSILVER = moddedCrushedOre(AllItems.CRUSHED_QUICKSILVER, "quicksilver", MW),
		CRUSHED_BAUXITE = moddedCrushedOre(AllItems.CRUSHED_BAUXITE, "aluminum", IE, SM),
		CRUSHED_URANIUM = moddedCrushedOre(AllItems.CRUSHED_URANIUM, "uranium", MEK, IE, SM),
		CRUSHED_NICKEL = moddedCrushedOre(AllItems.CRUSHED_NICKEL, "nickel", TH, IE, SM),

		ICE = convert(Blocks.ICE, Blocks.PACKED_ICE), MAGMA_BLOCK = convert(Blocks.MAGMA_BLOCK, Blocks.OBSIDIAN),

		WHITE_CONCRETE = convert(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE),
		ORANGE_CONCRETE = convert(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE),
		MAGENTA_CONCRETE = convert(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE),
		LIGHT_BLUE_CONCRETE = convert(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE),
		LIME_CONCRETE = convert(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE),
		YELLOW_CONCRETE = convert(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE),
		PINK_CONCRETE = convert(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE),
		LIGHT_GRAY_CONCRETE = convert(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE),
		GRAY_CONCRETE = convert(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE),
		PURPLE_CONCRETE = convert(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE),
		GREEN_CONCRETE = convert(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE),
		BROWN_CONCRETE = convert(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE),
		RED_CONCRETE = convert(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE),
		BLUE_CONCRETE = convert(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE),
		CYAN_CONCRETE = convert(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE),
		BLACK_CONCRETE = convert(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE),

		LIMESTONE = create(AllPaletteBlocks.LIMESTONE::get, b -> b.output(AllPaletteBlocks.WEATHERED_LIMESTONE.get())),
		FLOUR = create(AllItems.WHEAT_FLOUR::get, b -> b.output(AllItems.DOUGH.get()))

	;

	public GeneratedRecipe convert(Block block, Block result) {
		return create(() -> block, b -> b.output(result));
	}

	public GeneratedRecipe crushedOre(ItemEntry<Item> crushed, Supplier<ItemLike> nugget) {
		return create(crushed::get, b -> b.output(nugget.get(), 10)
			.output(.5f, nugget.get(), 5));
	}

	public GeneratedRecipe moddedCrushedOre(ItemEntry<? extends Item> crushed, String metalName, Mods... mods) {
		for (Mods mod : mods) {
			ResourceLocation nugget = mod.nuggetOf(metalName);
			create(mod.getId() + "/" + crushed.getId()
				.getPath(),
				b -> b.withItemIngredients(Ingredient.of(crushed::get))
					.output(1, nugget, 10)
					.output(.5f, nugget, 5)
					.whenModLoaded(mod.getId()));
		}
		return null;
	}

	public WashingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SPLASHING;
	}

}
