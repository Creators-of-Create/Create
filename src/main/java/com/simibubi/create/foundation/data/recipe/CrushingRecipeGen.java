package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.CompatMetals.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.LEAD;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.SILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.TIN;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.URANIUM;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

public class CrushingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	BLAZE_ROD = create(() -> Items.BLAZE_ROD, b -> b.duration(100)
		.output(Items.BLAZE_POWDER, 3)
		.output(.25f, Items.BLAZE_POWDER, 3)),

		PRISMARINE_CRYSTALS = create(() -> Items.PRISMARINE_CRYSTALS, b -> b.duration(150)
			.output(1f, Items.QUARTZ, 1)
			.output(.5f, Items.QUARTZ, 2)
			.output(.1f, Items.GLOWSTONE_DUST, 2)),

		OBSIDIAN = create(() -> Blocks.OBSIDIAN, b -> b.duration(500)
			.output(AllItems.POWDERED_OBSIDIAN.get())
			.output(.75f, Blocks.OBSIDIAN)),

		WOOL = create("wool", b -> b.duration(100)
			.require(ItemTags.WOOL)
			.output(Items.STRING, 2)
			.output(.5f, Items.STRING)),

		DIORITE = mineralRecycling(AllPaletteStoneTypes.DIORITE, b -> b.duration(350)
			.output(.25f, Items.QUARTZ, 1)),

		CRIMSITE =
			mineralRecycling(AllPaletteStoneTypes.CRIMSITE, AllItems.CRUSHED_IRON::get, () -> Items.IRON_NUGGET, .4f),

		VERIDIUM = mineralRecycling(AllPaletteStoneTypes.VERIDIUM, AllItems.CRUSHED_COPPER::get,
			() -> AllItems.COPPER_NUGGET::get, .8f),

		ASURINE = mineralRecycling(AllPaletteStoneTypes.ASURINE, AllItems.CRUSHED_ZINC::get,
			() -> AllItems.ZINC_NUGGET::get, .3f),

		OCHRUM =
			mineralRecycling(AllPaletteStoneTypes.OCHRUM, AllItems.CRUSHED_GOLD::get, () -> Items.GOLD_NUGGET, .2f),

		TUFF = mineralRecycling(AllPaletteStoneTypes.TUFF, b -> b.duration(350)
			.output(.25f, Items.FLINT, 1)
			.output(.1f, Items.GOLD_NUGGET, 1)
			.output(.1f, AllItems.COPPER_NUGGET.get(), 1)
			.output(.1f, AllItems.ZINC_NUGGET.get(), 1)
			.output(.1f, Items.IRON_NUGGET, 1)),

		COPPER_ORE = stoneOre(() -> Items.COPPER_ORE, AllItems.CRUSHED_COPPER::get, 5.25f, 250),
		ZINC_ORE = stoneOre(AllBlocks.ZINC_ORE::get, AllItems.CRUSHED_ZINC::get, 1.75f, 250),
		IRON_ORE = stoneOre(() -> Items.IRON_ORE, () -> AllItems.CRUSHED_IRON::get, 1.75f, 250),
		GOLD_ORE = stoneOre(() -> Items.GOLD_ORE, AllItems.CRUSHED_GOLD::get, 1.75f, 250),
		DIAMOND_ORE = stoneOre(() -> Items.DIAMOND_ORE, () -> Items.DIAMOND, 1.75f, 350),
		EMERALD_ORE = stoneOre(() -> Items.EMERALD_ORE, () -> Items.EMERALD, 1.75f, 350),
		COAL_ORE = stoneOre(() -> Items.COAL_ORE, () -> Items.COAL, 1.75f, 150),
		REDSTONE_ORE = stoneOre(() -> Items.REDSTONE_ORE, () -> Items.REDSTONE, 6.5f, 250),
		LAPIS_ORE = stoneOre(() -> Items.LAPIS_ORE, () -> Items.LAPIS_LAZULI, 10.5f, 250),

		DEEP_COPPER_ORE = deepslateOre(() -> Items.DEEPSLATE_COPPER_ORE, AllItems.CRUSHED_COPPER::get, 7.25f, 350),
		DEEP_ZINC_ORE = deepslateOre(AllBlocks.DEEPSLATE_ZINC_ORE::get, AllItems.CRUSHED_ZINC::get, 2.25f, 350),
		DEEP_IRON_ORE = deepslateOre(() -> Items.DEEPSLATE_IRON_ORE, AllItems.CRUSHED_IRON::get, 2.25f, 350),
		DEEP_GOLD_ORE = deepslateOre(() -> Items.DEEPSLATE_GOLD_ORE, AllItems.CRUSHED_GOLD::get, 2.25f, 350),
		DEEP_DIAMOND_ORE = deepslateOre(() -> Items.DEEPSLATE_DIAMOND_ORE, () -> Items.DIAMOND, 2.25f, 450),
		DEEP_EMERALD_ORE = deepslateOre(() -> Items.DEEPSLATE_EMERALD_ORE, () -> Items.EMERALD, 2.25f, 450),
		DEEP_COAL_ORE = deepslateOre(() -> Items.DEEPSLATE_COAL_ORE, () -> Items.COAL, 1.75f, 250),
		DEEP_REDSTONE_ORE = deepslateOre(() -> Items.DEEPSLATE_REDSTONE_ORE, () -> Items.REDSTONE, 7.5f, 350),
		DEEP_LAPIS_ORE = deepslateOre(() -> Items.DEEPSLATE_LAPIS_ORE, () -> Items.LAPIS_LAZULI, 12.5f, 350),

		NETHER_GOLD_ORE = netherOre(() -> Items.NETHER_GOLD_ORE, () -> Items.GOLD_NUGGET, 18, 350),
		NETHER_QUARTZ_ORE = netherOre(() -> Items.NETHER_QUARTZ_ORE, () -> Items.QUARTZ, 2.25f, 350),

		RAW_COPPER_ORE = rawOre(() -> Items.RAW_COPPER, AllItems.CRUSHED_COPPER::get, 1),
		RAW_ZINC_ORE = rawOre(AllItems.RAW_ZINC::get, AllItems.CRUSHED_ZINC::get, 1),
		RAW_IRON_ORE = rawOre(() -> Items.RAW_IRON, AllItems.CRUSHED_IRON::get, 1),
		RAW_GOLD_ORE = rawOre(() -> Items.RAW_GOLD, AllItems.CRUSHED_GOLD::get, 1),

		OSMIUM_ORE = moddedOre(OSMIUM, AllItems.CRUSHED_OSMIUM::get),
		PLATINUM_ORE = moddedOre(PLATINUM, AllItems.CRUSHED_PLATINUM::get),
		SILVER_ORE = moddedOre(SILVER, AllItems.CRUSHED_SILVER::get),
		TIN_ORE = moddedOre(TIN, AllItems.CRUSHED_TIN::get),
		QUICKSILVER_ORE = moddedOre(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get),
		LEAD_ORE = moddedOre(LEAD, AllItems.CRUSHED_LEAD::get),
		ALUMINUM_ORE = moddedOre(ALUMINUM, AllItems.CRUSHED_BAUXITE::get),
		URANIUM_ORE = moddedOre(URANIUM, AllItems.CRUSHED_URANIUM::get),
		NICKEL_ORE = moddedOre(NICKEL, AllItems.CRUSHED_NICKEL::get),

		OSMIUM_RAW_ORE = moddedRawOre(OSMIUM, AllItems.CRUSHED_OSMIUM::get, 1),
		PLATINUM_RAW_ORE = moddedRawOre(PLATINUM, AllItems.CRUSHED_PLATINUM::get, 1),
		SILVER_RAW_ORE = moddedRawOre(SILVER, AllItems.CRUSHED_SILVER::get, 1),
		TIN_RAW_ORE = moddedRawOre(TIN, AllItems.CRUSHED_TIN::get, 1),
		QUICKSILVER_RAW_ORE = moddedRawOre(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get, 1),
		LEAD_RAW_ORE = moddedRawOre(LEAD, AllItems.CRUSHED_LEAD::get, 1),
		ALUMINUM_RAW_ORE = moddedRawOre(ALUMINUM, AllItems.CRUSHED_BAUXITE::get, 1),
		URANIUM_RAW_ORE = moddedRawOre(URANIUM, AllItems.CRUSHED_URANIUM::get, 1),
		NICKEL_RAW_ORE = moddedRawOre(NICKEL, AllItems.CRUSHED_NICKEL::get, 1),

		RAW_COPPER_BLOCK = rawOre(() -> Items.RAW_COPPER_BLOCK, AllItems.CRUSHED_COPPER::get, 9),
		RAW_ZINC_BLOCK = rawOre(AllBlocks.RAW_ZINC_BLOCK::get, AllItems.CRUSHED_ZINC::get, 9),
		RAW_IRON_BLOCK = rawOre(() -> Items.RAW_IRON_BLOCK, AllItems.CRUSHED_IRON::get, 9),
		RAW_GOLD_BLOCK = rawOre(() -> Items.RAW_GOLD_BLOCK, AllItems.CRUSHED_GOLD::get, 9),

		OSMIUM_RAW_BLOCK = moddedRawOre(OSMIUM, AllItems.CRUSHED_OSMIUM::get, 9),
		PLATINUM_RAW_BLOCK = moddedRawOre(PLATINUM, AllItems.CRUSHED_PLATINUM::get, 9),
		SILVER_RAW_BLOCK = moddedRawOre(SILVER, AllItems.CRUSHED_SILVER::get, 9),
		TIN_RAW_BLOCK = moddedRawOre(TIN, AllItems.CRUSHED_TIN::get, 9),
		QUICKSILVER_RAW_BLOCK = moddedRawOre(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get, 9),
		LEAD_RAW_BLOCK = moddedRawOre(LEAD, AllItems.CRUSHED_LEAD::get, 9),
		ALUMINUM_RAW_BLOCK = moddedRawOre(ALUMINUM, AllItems.CRUSHED_BAUXITE::get, 9),
		URANIUM_RAW_BLOCK = moddedRawOre(URANIUM, AllItems.CRUSHED_URANIUM::get, 9),
		NICKEL_RAW_BLOCK = moddedRawOre(NICKEL, AllItems.CRUSHED_NICKEL::get, 9),

		NETHER_WART = create("nether_wart_block", b -> b.duration(150)
			.require(Blocks.NETHER_WART_BLOCK)
			.output(.25f, Items.NETHER_WART, 1)),

		AMETHYST_CLUSTER = create(() -> Blocks.AMETHYST_CLUSTER, b -> b.duration(150)
			.output(Items.AMETHYST_SHARD, 7)
			.output(.5f, Items.AMETHYST_SHARD)),

		GLOWSTONE = create(() -> Blocks.GLOWSTONE, b -> b.duration(150)
			.output(Items.GLOWSTONE_DUST, 3)
			.output(.5f, Items.GLOWSTONE_DUST)),

		AMETHYST_BLOCK = create(() -> Blocks.AMETHYST_BLOCK, b -> b.duration(150)
			.output(Items.AMETHYST_SHARD, 3)
			.output(.5f, Items.AMETHYST_SHARD)),

		LEATHER_HORSE_ARMOR = create(() -> Items.LEATHER_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.LEATHER, 2)
			.output(.5f, Items.LEATHER, 2)),

		IRON_HORSE_ARMOR = create(() -> Items.IRON_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.IRON_INGOT, 2)
			.output(.5f, Items.LEATHER, 1)
			.output(.5f, Items.IRON_INGOT, 1)
			.output(.25f, Items.STRING, 2)
			.output(.25f, Items.IRON_NUGGET, 4)),

		GOLDEN_HORSE_ARMOR = create(() -> Items.GOLDEN_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.GOLD_INGOT, 2)
			.output(.5f, Items.LEATHER, 2)
			.output(.5f, Items.GOLD_INGOT, 2)
			.output(.25f, Items.STRING, 2)
			.output(.25f, Items.GOLD_NUGGET, 8)),

		DIAMOND_HORSE_ARMOR = create(() -> Items.DIAMOND_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.DIAMOND, 1)
			.output(.5f, Items.LEATHER, 2)
			.output(.1f, Items.DIAMOND, 3)
			.output(.25f, Items.STRING, 2)),

		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.duration(250)
			.output(Blocks.SAND)
			.output(.1f, Items.FLINT)
			.output(.05f, Items.CLAY_BALL)),

		NETHERRACK = create(() -> Blocks.NETHERRACK, b -> b.duration(250)
			.output(AllItems.CINDER_FLOUR.get())
			.output(.5f, AllItems.CINDER_FLOUR.get()))

	;

	protected GeneratedRecipe stoneOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.COBBLESTONE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe deepslateOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.COBBLED_DEEPSLATE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe netherOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.NETHERRACK, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type, Supplier<ItemLike> crushed,
		Supplier<ItemLike> nugget, float chance) {
		return mineralRecycling(type, b -> b.duration(250)
			.output(chance, crushed.get(), 1)
			.output(chance, nugget.get(), 1));
	}

	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type,
		UnaryOperator<ProcessingRecipeBuilder<ProcessingRecipe<?>>> transform) {
		create(Lang.asId(type.name()) + "_recycling", b -> transform.apply(b.require(type.materialTag)));
		return create(type.getBaseBlock()::get, transform);
	}

	protected GeneratedRecipe ore(ItemLike stoneType, Supplier<ItemLike> ore, Supplier<ItemLike> raw,
		float expectedAmount, int duration) {
		return create(ore, b -> {
			ProcessingRecipeBuilder<ProcessingRecipe<?>> builder = b.duration(duration)
				.output(raw.get(), Mth.floor(expectedAmount));
			float extra = expectedAmount - Mth.floor(expectedAmount);
			if (extra > 0)
				builder.output(extra, raw.get(), 1);
			builder.output(.75f, AllItems.EXP_NUGGET.get(), raw.get() == AllItems.CRUSHED_GOLD.get() ? 2 : 1);
			return builder.output(.125f, stoneType);
		});
	}

	protected GeneratedRecipe rawOre(Supplier<ItemLike> input, Supplier<ItemLike> result, int amount) {
		return create(input, b -> b.duration(400)
			.output(result.get(), amount)
			.output(.75f, AllItems.EXP_NUGGET.get(), (result.get() == AllItems.CRUSHED_GOLD.get() ? 2 : 1) * amount));
	}

	protected GeneratedRecipe moddedRawOre(CompatMetals metal, Supplier<ItemLike> result, int amount) {
		String name = metal.getName();
		return create("raw_" + name + (amount == 1 ? "_ore" : "_block"), b -> {
			String prefix = amount == 1 ? "raw_materials/" : "storage_blocks/raw_";
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition("forge", prefix + name)))
				.require(AllTags.forgeItemTag(prefix + name))
				.output(result.get(), amount)
				.output(.75f, AllItems.EXP_NUGGET.get(), amount);
		});
	}

	protected GeneratedRecipe moddedOre(CompatMetals metal, Supplier<ItemLike> result) {
		String name = metal.getName();
		return create(name + "_ore", b -> {
			String prefix = "ores/";
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition("forge", prefix + name)))
				.require(AllTags.forgeItemTag(prefix + name))
				.output(result.get(), 1)
				.output(.75f, result.get(), 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1);
		});
	}

	public CrushingRecipeGen(PackOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CRUSHING;
	}

}
