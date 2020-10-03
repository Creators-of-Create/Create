package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.content.palettes.ConnectedGlassBlock;
import com.simibubi.create.content.palettes.ConnectedGlassPaneBlock;
import com.simibubi.create.content.palettes.GlassPaneBlock;
import com.simibubi.create.content.palettes.WindowBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;

public class WindowGen {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock) {
		return woodenWindowBlock(woodType, planksBlock, () -> RenderType::getCutoutMipped);
	}

	public static BlockEntry<WindowBlock> customWindowBlock(String name, Supplier<? extends IItemProvider> ingredient,
		CTSpriteShiftEntry ct, Supplier<Supplier<RenderType>> renderType) {
		NonNullFunction<String, ResourceLocation> end_texture = n -> Create.asResource(palettesDir() + name + "_end");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ingredient, ct, renderType, end_texture, side_texture);
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock,
		Supplier<Supplier<RenderType>> renderType) {
		String woodName = woodType.getName();
		String name = woodName + "_window";
		NonNullFunction<String, ResourceLocation> end_texture =
			$ -> new ResourceLocation("block/" + woodName + "_planks");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, () -> planksBlock, AllSpriteShifts.getWoodenWindow(woodType), renderType, end_texture,
			side_texture);
	}

	public static BlockEntry<WindowBlock> windowBlock(String name, Supplier<? extends IItemProvider> ingredient,
		CTSpriteShiftEntry ct, Supplier<Supplier<RenderType>> renderType,
		NonNullFunction<String, ResourceLocation> endTexture, NonNullFunction<String, ResourceLocation> sideTexture) {
		return REGISTRATE.block(name, WindowBlock::new)
			.onRegister(connectedTextures(new HorizontalCTBehaviour(ct)))
			.addLayer(renderType)
			.recipe((c, p) -> ShapedRecipeBuilder.shapedRecipe(c.get(), 2)
				.patternLine(" # ")
				.patternLine("#X#")
				.key('#', ingredient.get())
				.key('X', DataIngredient.tag(Tags.Items.GLASS_COLORLESS))
				.addCriterion("has_ingredient", p.hasItem(ingredient.get()))
				.build(p::accept))
			.initialProperties(() -> Blocks.GLASS)
			.loot((t, g) -> t.registerSilkTouch(g))
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), sideTexture.apply(c.getName()), endTexture.apply(c.getName()))))
			.simpleItem()
			.register();
	}

	public static BlockEntry<ConnectedGlassBlock> framedGlass(String name, ConnectedTextureBehaviour behaviour) {
		return REGISTRATE.block(name, ConnectedGlassBlock::new)
			.onRegister(connectedTextures(behaviour))
			.addLayer(() -> RenderType::getTranslucent)
			.initialProperties(() -> Blocks.GLASS)
			.loot((t, g) -> t.registerSilkTouch(g))
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_COLORLESS), c::get))
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
			.tag(Tags.Blocks.GLASS_COLORLESS)
			.item()
			.tag(Tags.Items.GLASS_COLORLESS)
			.model((c, p) -> p.cubeColumn(c.getName(), p.modLoc(palettesDir() + c.getName()),
				p.modLoc("block/palettes/framed_glass")))
			.build()
			.register();
	}

	public static BlockEntry<ConnectedGlassPaneBlock> framedGlassPane(String name, Supplier<? extends Block> parent,
		CTSpriteShiftEntry ctshift) {
		ResourceLocation sideTexture = Create.asResource(palettesDir() + "framed_glass");
		ResourceLocation itemSideTexture = Create.asResource(palettesDir() + name);
		ResourceLocation topTexture = Create.asResource(palettesDir() + "framed_glass_pane_top");
		Supplier<Supplier<RenderType>> renderType = () -> RenderType::getTranslucent;
		return connectedGlassPane(name, parent, ctshift, sideTexture, itemSideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> customWindowPane(String name, Supplier<? extends Block> parent,
		CTSpriteShiftEntry ctshift, Supplier<Supplier<RenderType>> renderType) {
		ResourceLocation topTexture = Create.asResource(palettesDir() + name + "_pane_top");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, ctshift, sideTexture, sideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
		Supplier<? extends Block> parent) {
		return woodenWindowPane(woodType, parent, () -> RenderType::getCutoutMipped);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
		Supplier<? extends Block> parent, Supplier<Supplier<RenderType>> renderType) {
		String woodName = woodType.getName();
		String name = woodName + "_window";
		ResourceLocation topTexture = new ResourceLocation("block/" + woodName + "_planks");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, AllSpriteShifts.getWoodenWindow(woodType), sideTexture, sideTexture,
			topTexture, renderType);
	}

	public static BlockEntry<GlassPaneBlock> standardGlassPane(String name, Supplier<? extends Block> parent,
		ResourceLocation sideTexture, ResourceLocation topTexture, Supplier<Supplier<RenderType>> renderType) {
		NonNullBiConsumer<DataGenContext<Block, GlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), sideTexture, topTexture);
		return glassPane(name, parent, sideTexture, topTexture, GlassPaneBlock::new, renderType, $ -> {
		}, stateProvider);
	}

	private static BlockEntry<ConnectedGlassPaneBlock> connectedGlassPane(String name, Supplier<? extends Block> parent,
		CTSpriteShiftEntry ctshift, ResourceLocation sideTexture, ResourceLocation itemSideTexture,
		ResourceLocation topTexture, Supplier<Supplier<RenderType>> renderType) {
		NonNullConsumer<? super ConnectedGlassPaneBlock> connectedTextures =
			connectedTextures(new GlassPaneCTBehaviour(ctshift));
		String CGPparents = "block/connected_glass_pane/";
		String prefix = name + "_pane_";

		Function<RegistrateBlockstateProvider, ModelFile> post =
			getPaneModelProvider(CGPparents, prefix, "post", sideTexture, topTexture),
			side = getPaneModelProvider(CGPparents, prefix, "side", sideTexture, topTexture),
			sideAlt = getPaneModelProvider(CGPparents, prefix, "side_alt", sideTexture, topTexture),
			noSide = getPaneModelProvider(CGPparents, prefix, "noside", sideTexture, topTexture),
			noSideAlt = getPaneModelProvider(CGPparents, prefix, "noside_alt", sideTexture, topTexture);

		NonNullBiConsumer<DataGenContext<Block, ConnectedGlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), post.apply(p), side.apply(p), sideAlt.apply(p), noSide.apply(p),
				noSideAlt.apply(p));

		return glassPane(name, parent, itemSideTexture, topTexture, ConnectedGlassPaneBlock::new, renderType,
			connectedTextures, stateProvider);
	}

	private static Function<RegistrateBlockstateProvider, ModelFile> getPaneModelProvider(String CGPparents,
		String prefix, String partial, ResourceLocation sideTexture, ResourceLocation topTexture) {
		return p -> p.models()
			.withExistingParent(prefix + partial, Create.asResource(CGPparents + partial))
			.texture("pane", sideTexture)
			.texture("edge", topTexture);
	}

	private static <G extends GlassPaneBlock> BlockEntry<G> glassPane(String name, Supplier<? extends Block> parent,
		ResourceLocation sideTexture, ResourceLocation topTexture, NonNullFunction<Properties, G> factory,
		Supplier<Supplier<RenderType>> renderType, NonNullConsumer<? super G> connectedTextures,
		NonNullBiConsumer<DataGenContext<Block, G>, RegistrateBlockstateProvider> stateProvider) {
		name += "_pane";

		return REGISTRATE.block(name, factory)
			.onRegister(connectedTextures)
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS_PANE)
			.blockstate(stateProvider)
			.recipe((c, p) -> ShapedRecipeBuilder.shapedRecipe(c.get(), 16)
				.patternLine("###")
				.patternLine("###")
				.key('#', parent.get())
				.addCriterion("has_ingredient", p.hasItem(parent.get()))
				.build(p::accept))
			.tag(Tags.Blocks.GLASS_PANES)
			.loot((t, g) -> t.registerSilkTouch(g))
			.item()
			.tag(Tags.Items.GLASS_PANES)
			.model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Create.ID, "item/pane"))
				.texture("pane", sideTexture)
				.texture("edge", topTexture))
			.build()
			.register();
	}

	private static String palettesDir() {
		return "block/palettes/";
	}

}
