package com.simibubi.create.modules.palettes;

import static com.simibubi.create.modules.palettes.PaletteBlockPartial.AllPartials;
import static com.simibubi.create.modules.palettes.PaletteBlockPartial.ForPolished;
import static com.simibubi.create.modules.palettes.PatternNameType.Prefix;
import static com.simibubi.create.modules.palettes.PatternNameType.Suffix;
import static com.simibubi.create.modules.palettes.PatternNameType.Wrap;

import java.util.Optional;
import java.util.function.Function;

import com.simibubi.create.Create;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.foundation.utility.data.ModelGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ModelFile;

public class PaletteBlockPatterns {
	
	public static final PaletteBlockPatterns 
		COBBLESTONE = create("cobblestone", Suffix, AllPartials),
		POLISHED = create("polished", Prefix, ForPolished), 
		BRICKS = create("bricks", Suffix, AllPartials),
		FANCY_BRICKS = create("fancy_bricks", Wrap, AllPartials),

		PAVED = create("paved", Prefix, AllPartials).blockStateFactory(p -> p::paved)
			.block(PavedBlock::new)
			.textures("paved", "paved_borderless", "paved_top"),

		LAYERED = create("layered", Prefix).blockStateFactory(p -> p::cubeColumn)
			.textures("layered", "polished")
			.connectedTextures(v -> new HorizontalCTBehaviour(
				ct(v, CTs.LAYERED), 
				ct(v, CTs.POLISHED))),

		CHISELED = create("chiseled", Prefix).blockStateFactory(p -> p::cubeColumn)
			.textures("chiseled", "chiseled_top"),

		PILLAR = create("pillar", Suffix).blockStateFactory(p -> p::pillar)
			.block(RotatedPillarBlock::new)
			.textures("pillar", "pillar_end"),

		MOSSY = create("mossy", Prefix).blockStateFactory(p -> p::cubeAllButMossy)
			.block(MossyBlock::new)
			.textures("bricks", "mossy")
			.useTranslucentLayer(),

		OVERGROWN = create("overgrown", Prefix).blockStateFactory(p -> p::cubeAllButMossy)
			.block(MossyBlock::new)
			.textures("bricks", "overgrown")
			.useTranslucentLayer()

	;

	public static final PaletteBlockPatterns[] vanillaRange =
		{ COBBLESTONE, BRICKS, FANCY_BRICKS, PILLAR, PAVED, LAYERED, MOSSY, OVERGROWN };

	public static final PaletteBlockPatterns[] standardRange =
		{ COBBLESTONE, POLISHED, BRICKS, FANCY_BRICKS, PILLAR, PAVED, LAYERED, CHISELED, MOSSY, OVERGROWN };

	static final String textureLocation = "block/palettes/%s/%s";
	static final String overlayLocation = "block/palettes/%s";

	private PatternNameType nameType;
	private String[] textures;
	private String id;
	private boolean isTranslucent;
	private Optional<Function<PaletteStoneVariants, ConnectedTextureBehaviour>> ctBehaviour;

	private IPatternBlockStateGenerator blockStateGenerator;
	private NonNullFunction<Properties, ? extends Block> blockFactory;
	private PaletteBlockPartial<? extends Block>[] partials;

	@OnlyIn(Dist.CLIENT)
	private RenderType renderType;

	private static PaletteBlockPatterns create(String name, PatternNameType nameType,
		PaletteBlockPartial<?>... partials) {
		PaletteBlockPatterns pattern = new PaletteBlockPatterns();
		pattern.id = name;
		pattern.ctBehaviour = Optional.empty();
		pattern.nameType = nameType;
		pattern.partials = partials;
		pattern.isTranslucent = false;
		pattern.blockFactory = Block::new;
		pattern.textures = new String[] { name };
		pattern.blockStateGenerator = p -> p::cubeAll;
		return pattern;
	}

	public IPatternBlockStateGenerator getBlockStateGenerator() {
		return blockStateGenerator;
	}

	public boolean isTranslucent() {
		return isTranslucent;
	}

	public NonNullFunction<Properties, ? extends Block> getBlockFactory() {
		return blockFactory;
	}

	public PaletteBlockPartial<? extends Block>[] getPartials() {
		return partials;
	}
	
	public String getTextureForPartials() {
		return textures[0];
	}
	
	public Optional<ConnectedTextureBehaviour> createCTBehaviour(PaletteStoneVariants variant) {
		return ctBehaviour.map(f -> f.apply(variant));
	}

	// Builder

	private PaletteBlockPatterns blockStateFactory(IPatternBlockStateGenerator factory) {
		blockStateGenerator = factory;
		return this;
	}

	private PaletteBlockPatterns textures(String... textures) {
		this.textures = textures;
		return this;
	}

	private PaletteBlockPatterns block(NonNullFunction<Properties, ? extends Block> blockFactory) {
		this.blockFactory = blockFactory;
		return this;
	}

	private PaletteBlockPatterns useTranslucentLayer() {
		isTranslucent = true;
		return this;
	}
	
	private PaletteBlockPatterns connectedTextures(Function<PaletteStoneVariants, ConnectedTextureBehaviour> factory) {
		this.ctBehaviour = Optional.of(factory);
		return this;
	}
	
	// Model generators

	public IBlockStateProvider cubeAll(String variant) {
		ResourceLocation all = toLocation(variant, textures[0]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeAll(createName(variant), all));
	}

	public IBlockStateProvider cubeAllButMossy(String variant) {
		ResourceLocation all = toLocation(variant, textures[0]);
		ResourceLocation overlay = toOverlayLocation(textures[1]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), ModelGen.createOvergrown(ctx, prov, all, overlay));
	}

	public IBlockStateProvider cubeBottomTop(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation bottom = toLocation(variant, textures[1]);
		ResourceLocation top = toLocation(variant, textures[2]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeBottomTop(createName(variant), side, bottom, top));
	}

	public IBlockStateProvider pillar(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation end = toLocation(variant, textures[1]);
		return (ctx, prov) -> BlockStateGen.axisBlock(ctx, prov, $ -> prov.models()
			.cubeColumn(createName(variant), side, end));
	}

	public IBlockStateProvider cubeColumn(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation end = toLocation(variant, textures[1]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeColumn(createName(variant), side, end));
	}

	public IBlockStateProvider paved(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation bottom = toLocation(variant, textures[1]);
		ResourceLocation top = toLocation(variant, textures[2]);
		return (ctx, prov) -> {
			ModelFile cubeBottomTop = prov.models()
				.cubeBottomTop(createName(variant), side, bottom, top);
			ModelFile cubeAll = prov.models()
				.cubeAll(createName(variant) + "_covered", bottom);
			BlockStateGen.pavedBlock(ctx, prov, cubeBottomTop, cubeAll);
		};
	}

	// Utility

	protected String createName(String variant) {
		if (nameType == Wrap) {
			String[] split = id.split("_");
			if (split.length == 2) {
				String formatString = "%s_%s_%s";
				return String.format(formatString, split[0], variant, split[1]);
			}
		}
		String formatString = "%s_%s";
		return nameType == Suffix ? String.format(formatString, variant, id) : String.format(formatString, id, variant);
	}

	protected ResourceLocation toLocation(String variant, String texture) {
		return Create.asResource(String.format(textureLocation, variant, texture));
	}

	protected ResourceLocation toOverlayLocation(String texture) {
		return Create.asResource(String.format(overlayLocation, texture));
	}

	protected static CTSpriteShiftEntry ct(PaletteStoneVariants variant, CTs texture) {
		return AllSpriteShifts.getVariantPattern(variant, texture);
	}
	
	@FunctionalInterface
	static interface IPatternBlockStateGenerator
		extends Function<PaletteBlockPatterns, Function<String, IBlockStateProvider>> {
	}

	@FunctionalInterface
	static interface IBlockStateProvider
		extends NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> {
	}
	
	// Textures with connectability, used by Spriteshifter
	
	public static enum CTs {
		
		POLISHED(CTType.OMNIDIRECTIONAL), 
		LAYERED(CTType.HORIZONTAL)
		
		;
		
		public CTType type;
		private CTs(CTType type) {
			this.type = type;
		}
		
	}

}
