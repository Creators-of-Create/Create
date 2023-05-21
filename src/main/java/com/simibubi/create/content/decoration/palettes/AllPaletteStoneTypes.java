package com.simibubi.create.content.decoration.palettes;

import static com.simibubi.create.content.decoration.palettes.PaletteBlockPattern.STANDARD_RANGE;
import static com.simibubi.create.content.decoration.palettes.PaletteBlockPattern.VANILLA_RANGE;

import java.util.function.Function;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;

public enum AllPaletteStoneTypes {

	GRANITE(VANILLA_RANGE, r -> () -> Blocks.GRANITE),
	DIORITE(VANILLA_RANGE, r -> () -> Blocks.DIORITE),
	ANDESITE(VANILLA_RANGE, r -> () -> Blocks.ANDESITE),
	CALCITE(VANILLA_RANGE, r -> () -> Blocks.CALCITE),
	DRIPSTONE(VANILLA_RANGE, r -> () -> Blocks.DRIPSTONE_BLOCK),
	DEEPSLATE(VANILLA_RANGE, r -> () -> Blocks.DEEPSLATE),
	TUFF(VANILLA_RANGE, r -> () -> Blocks.TUFF),

	ASURINE(STANDARD_RANGE, r -> r.paletteStoneBlock("asurine", () -> Blocks.DEEPSLATE, true, true)
		.properties(p -> p.destroyTime(1.25f)
			.color(MaterialColor.COLOR_BLUE))
		.register()),

	CRIMSITE(STANDARD_RANGE, r -> r.paletteStoneBlock("crimsite", () -> Blocks.DEEPSLATE, true, true)
		.properties(p -> p.destroyTime(1.25f)
			.color(MaterialColor.COLOR_RED))
		.register()),

	LIMESTONE(STANDARD_RANGE, r -> r.paletteStoneBlock("limestone", () -> Blocks.SANDSTONE, true, false)
		.properties(p -> p.destroyTime(1.25f)
			.color(MaterialColor.SAND))
		.register()),

	OCHRUM(STANDARD_RANGE, r -> r.paletteStoneBlock("ochrum", () -> Blocks.CALCITE, true, true)
		.properties(p -> p.destroyTime(1.25f)
			.color(MaterialColor.TERRACOTTA_YELLOW))
		.register()),

	SCORIA(STANDARD_RANGE, r -> r.paletteStoneBlock("scoria", () -> Blocks.BLACKSTONE, true, false)
		.properties(p -> p.color(MaterialColor.COLOR_BROWN))
		.register()),

	SCORCHIA(STANDARD_RANGE, r -> r.paletteStoneBlock("scorchia", () -> Blocks.BLACKSTONE, true, false)
		.properties(p -> p.color(MaterialColor.TERRACOTTA_GRAY))
		.register()),

	VERIDIUM(STANDARD_RANGE, r -> r.paletteStoneBlock("veridium", () -> Blocks.TUFF, true, true)
		.properties(p -> p.destroyTime(1.25f)
			.color(MaterialColor.WARPED_NYLIUM))
		.register())

	;

	private Function<CreateRegistrate, NonNullSupplier<Block>> factory;
	private PalettesVariantEntry variants;

	public NonNullSupplier<Block> baseBlock;
	public PaletteBlockPattern[] variantTypes;
	public TagKey<Item> materialTag;

	private AllPaletteStoneTypes(PaletteBlockPattern[] variantTypes,
		Function<CreateRegistrate, NonNullSupplier<Block>> factory) {
		this.factory = factory;
		this.variantTypes = variantTypes;
	}

	public NonNullSupplier<Block> getBaseBlock() {
		return baseBlock;
	}

	public PalettesVariantEntry getVariants() {
		return variants;
	}

	public static void register(CreateRegistrate registrate) {
		for (AllPaletteStoneTypes paletteStoneVariants : values()) {
			NonNullSupplier<Block> baseBlock = paletteStoneVariants.factory.apply(registrate);
			paletteStoneVariants.baseBlock = baseBlock;
			String id = Lang.asId(paletteStoneVariants.name());
			paletteStoneVariants.materialTag =
				AllTags.optionalTag(ForgeRegistries.ITEMS, Create.asResource("stone_types/" + id));
			paletteStoneVariants.variants = new PalettesVariantEntry(id, paletteStoneVariants);
		}
	}

}
