package com.simibubi.create.content.palettes;

import static com.simibubi.create.content.palettes.PaletteBlockPattern.STANDARD_RANGE;
import static com.simibubi.create.content.palettes.PaletteBlockPattern.VANILLA_RANGE;

import java.util.function.Function;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum AllPaletteStoneTypes {

	GRANITE(VANILLA_RANGE, r -> () -> Blocks.GRANITE),
	DIORITE(VANILLA_RANGE, r -> () -> Blocks.DIORITE),
	ANDESITE(VANILLA_RANGE, r -> () -> Blocks.ANDESITE),
	CALCITE(VANILLA_RANGE, r -> () -> Blocks.CALCITE),
	DRIPSTONE(VANILLA_RANGE, r -> () -> Blocks.DRIPSTONE_BLOCK),
	DEEPSLATE(VANILLA_RANGE, r -> () -> Blocks.DEEPSLATE),
	TUFF(VANILLA_RANGE, r -> () -> Blocks.TUFF),

	ASURINE(STANDARD_RANGE, r -> r.paletteStoneBlock("asurine", () -> Blocks.DEEPSLATE, true)
		.properties(p -> p.destroyTime(1.25f))
		.register()),

	CRIMSITE(STANDARD_RANGE, r -> r.paletteStoneBlock("crimsite", () -> Blocks.DEEPSLATE, true)
		.properties(p -> p.destroyTime(1.25f))
		.register()),

	LIMESTONE(STANDARD_RANGE, r -> r.paletteStoneBlock("limestone", () -> Blocks.SANDSTONE, true)
		.properties(p -> p.destroyTime(1.25f))
		.register()),

	OCHRUM(STANDARD_RANGE, r -> r.paletteStoneBlock("ochrum", () -> Blocks.CALCITE, true)
		.properties(p -> p.destroyTime(1.25f))
		.register()),

	SCORIA(STANDARD_RANGE, r -> r.paletteStoneBlock("scoria", () -> Blocks.BLACKSTONE, true)
		.register()),

	SCORCHIA(STANDARD_RANGE, r -> r.paletteStoneBlock("scorchia", () -> Blocks.BLACKSTONE, true)
		.register()),

	VERIDIUM(STANDARD_RANGE, r -> r.paletteStoneBlock("veridium", () -> Blocks.TUFF, true)
		.properties(p -> p.destroyTime(1.25f))
		.register())

	;

	private Function<CreateRegistrate, NonNullSupplier<Block>> factory;
	private PalettesVariantEntry variants;

	public NonNullSupplier<Block> baseBlock;
	public PaletteBlockPattern[] variantTypes;
	public Tag.Named<Item> materialTag;

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
			paletteStoneVariants.materialTag = AllTags.tag(TagFactory.ITEM::create, Create.ID, "stone_types/" + id);
			paletteStoneVariants.variants = new PalettesVariantEntry(id, paletteStoneVariants);
		}
	}

}
