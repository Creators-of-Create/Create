package com.simibubi.create.content.decoration.palettes;

import static com.simibubi.create.foundation.data.WindowGen.customWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.customWindowPane;
import static com.simibubi.create.foundation.data.WindowGen.framedGlass;
import static com.simibubi.create.foundation.data.WindowGen.framedGlassPane;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowPane;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.WindowGen;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.common.Tags;

public class AllPaletteBlocks {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.PALETTES_CREATIVE_TAB);
	}

	// Windows and Glass

	public static final BlockEntry<TransparentBlock> TILED_GLASS = REGISTRATE.block("tiled_glass", TransparentBlock::new)
		.initialProperties(() -> Blocks.GLASS)
		.addLayer(() -> RenderType::cutout)
		.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_BLOCKS_COLORLESS), RecipeCategory.BUILDING_BLOCKS, c))
		.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/"))
		.loot((t, g) -> t.dropWhenSilkTouch(g))
		.tag(Tags.Blocks.GLASS_BLOCKS_COLORLESS, BlockTags.IMPERMEABLE)
		.item()
		.tag(Tags.Items.GLASS_BLOCKS_COLORLESS)
		.build()
		.register();

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		framedGlass("framed_glass", () -> new SimpleCTBehaviour(AllSpriteShifts.FRAMED_GLASS)),
		HORIZONTAL_FRAMED_GLASS = framedGlass("horizontal_framed_glass",
			() -> new HorizontalCTBehaviour(AllSpriteShifts.HORIZONTAL_FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS)),
		VERTICAL_FRAMED_GLASS = framedGlass("vertical_framed_glass",
			() -> new HorizontalCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS));

	public static final BlockEntry<GlassPaneBlock> TILED_GLASS_PANE =
		WindowGen.standardGlassPane("tiled_glass", TILED_GLASS, Create.asResource("block/palettes/tiled_glass"),
			ResourceLocation.withDefaultNamespace("block/glass_pane_top"), () -> RenderType::cutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> FRAMED_GLASS_PANE =
		framedGlassPane("framed_glass", FRAMED_GLASS, () -> AllSpriteShifts.FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS_PANE = framedGlassPane("horizontal_framed_glass", HORIZONTAL_FRAMED_GLASS,
			() -> AllSpriteShifts.HORIZONTAL_FRAMED_GLASS),
		VERTICAL_FRAMED_GLASS_PANE = framedGlassPane("vertical_framed_glass", VERTICAL_FRAMED_GLASS,
			() -> AllSpriteShifts.VERTICAL_FRAMED_GLASS);

	public static final BlockEntry<WindowBlock> OAK_WINDOW = woodenWindowBlock(WoodType.OAK, Blocks.OAK_PLANKS),
		SPRUCE_WINDOW = woodenWindowBlock(WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
		BIRCH_WINDOW = woodenWindowBlock(WoodType.BIRCH, Blocks.BIRCH_PLANKS, () -> RenderType::translucent, true),
		JUNGLE_WINDOW = woodenWindowBlock(WoodType.JUNGLE, Blocks.JUNGLE_PLANKS),
		ACACIA_WINDOW = woodenWindowBlock(WoodType.ACACIA, Blocks.ACACIA_PLANKS),
		DARK_OAK_WINDOW = woodenWindowBlock(WoodType.DARK_OAK, Blocks.DARK_OAK_PLANKS),
		MANGROVE_WINDOW = woodenWindowBlock(WoodType.MANGROVE, Blocks.MANGROVE_PLANKS),
		CRIMSON_WINDOW = woodenWindowBlock(WoodType.CRIMSON, Blocks.CRIMSON_PLANKS),
		WARPED_WINDOW = woodenWindowBlock(WoodType.WARPED, Blocks.WARPED_PLANKS),
		CHERRY_WINDOW = woodenWindowBlock(WoodType.CHERRY, Blocks.CHERRY_PLANKS),
		BAMBOO_WINDOW = woodenWindowBlock(WoodType.BAMBOO, Blocks.BAMBOO_PLANKS),
		ORNATE_IRON_WINDOW =
			customWindowBlock("ornate_iron_window", () -> Items.IRON_NUGGET, () -> AllSpriteShifts.ORNATE_IRON_WINDOW,
				() -> RenderType::cutout, false, () -> MapColor.TERRACOTTA_LIGHT_GRAY),
		INDUSTRIAL_IRON_WINDOW = customWindowBlock("industrial_iron_window", AllBlocks.INDUSTRIAL_IRON_BLOCK,
			() -> AllSpriteShifts.INDUSTRIAL_IRON_WINDOW, () -> RenderType::cutout, false, () -> MapColor.COLOR_GRAY),
		WEATHERED_IRON_WINDOW = WindowGen
			.randomisedWindowBlock("weathered_iron_window", AllBlocks.WEATHERED_IRON_BLOCK,
				() -> RenderType::translucent, true, () -> MapColor.TERRACOTTA_LIGHT_GRAY)
			.onRegister(CreateRegistrate.connectedTextures(() -> new WeatheredIronWindowCTBehaviour()))
			.register();

	public static final BlockEntry<ConnectedGlassPaneBlock> OAK_WINDOW_PANE =
		woodenWindowPane(WoodType.OAK, OAK_WINDOW),
		SPRUCE_WINDOW_PANE = woodenWindowPane(WoodType.SPRUCE, SPRUCE_WINDOW),
		BIRCH_WINDOW_PANE = woodenWindowPane(WoodType.BIRCH, BIRCH_WINDOW, () -> RenderType::translucent),
		JUNGLE_WINDOW_PANE = woodenWindowPane(WoodType.JUNGLE, JUNGLE_WINDOW),
		ACACIA_WINDOW_PANE = woodenWindowPane(WoodType.ACACIA, ACACIA_WINDOW),
		DARK_OAK_WINDOW_PANE = woodenWindowPane(WoodType.DARK_OAK, DARK_OAK_WINDOW),
		MANGROVE_WINDOW_PANE = woodenWindowPane(WoodType.MANGROVE, MANGROVE_WINDOW),
		CRIMSON_WINDOW_PANE = woodenWindowPane(WoodType.CRIMSON, CRIMSON_WINDOW),
		WARPED_WINDOW_PANE = woodenWindowPane(WoodType.WARPED, WARPED_WINDOW),
		CHERRY_WINDOW_PANE = woodenWindowPane(WoodType.CHERRY, CHERRY_WINDOW),
		BAMBOO_WINDOW_PANE = woodenWindowPane(WoodType.BAMBOO, BAMBOO_WINDOW),
		ORNATE_IRON_WINDOW_PANE = customWindowPane("ornate_iron_window", ORNATE_IRON_WINDOW,
			() -> AllSpriteShifts.ORNATE_IRON_WINDOW, () -> RenderType::cutoutMipped).register(),
		INDUSTRIAL_IRON_WINDOW_PANE = customWindowPane("industrial_iron_window", INDUSTRIAL_IRON_WINDOW,
			() -> AllSpriteShifts.INDUSTRIAL_IRON_WINDOW, () -> RenderType::cutoutMipped).register(),
		WEATHERED_IRON_WINDOW_PANE =
			customWindowPane("weathered_iron_window", WEATHERED_IRON_WINDOW, null, () -> RenderType::translucent)
				.onRegister(CreateRegistrate.connectedTextures(() -> new WeatheredIronWindowPaneCTBehaviour()))
				.register();

	static {
		AllPaletteStoneTypes.register(REGISTRATE);
	}

	public static void register() {
	}

}
