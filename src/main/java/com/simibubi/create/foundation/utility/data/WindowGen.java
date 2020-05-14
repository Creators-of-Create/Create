package com.simibubi.create.foundation.utility.data;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.AllCTs;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.modules.palettes.ConnectedGlassBlock;
import com.simibubi.create.modules.palettes.ConnectedGlassPaneBlock;
import com.simibubi.create.modules.palettes.GlassPaneBlock;
import com.simibubi.create.modules.palettes.PalettesRegistrate;
import com.simibubi.create.modules.palettes.WindowBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;

public class WindowGen {

	private static final PalettesRegistrate REGISTRATE = Create.palettesRegistrate();

	public static BlockEntry<WindowBlock> woodenWindowBlock(String woodType, AllCTs ct) {
		return woodenWindowBlock(woodType, ct, () -> RenderType::getCutoutMipped);
	}

	public static BlockEntry<WindowBlock> customWindowBlock(String name, AllCTs ct,
		Supplier<Supplier<RenderType>> renderType) {
		NonNullFunction<String, ResourceLocation> end_texture = n -> Create.asResource(palettesDir() + name + "_end");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ct, renderType, end_texture, side_texture);
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(String woodType, AllCTs ct,
		Supplier<Supplier<RenderType>> renderType) {
		String name = woodType + "_window";
		NonNullFunction<String, ResourceLocation> end_texture =
			$ -> new ResourceLocation("block/" + woodType + "_planks");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ct, renderType, end_texture, side_texture);
	}

	public static BlockEntry<WindowBlock> windowBlock(String name, AllCTs ct, Supplier<Supplier<RenderType>> renderType,
		NonNullFunction<String, ResourceLocation> endTexture, NonNullFunction<String, ResourceLocation> sideTexture) {
		return REGISTRATE.block(name, WindowBlock::new)
			.transform(REGISTRATE.connectedTextures(new HorizontalCTBehaviour(ct.get())))
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), sideTexture.apply(c.getName()), endTexture.apply(c.getName()))))
			.simpleItem()
			.register();
	}

	public static BlockEntry<ConnectedGlassBlock> framedGlass(String name, ConnectedTextureBehaviour behaviour) {
		return REGISTRATE.block(name, ConnectedGlassBlock::new)
			.transform(REGISTRATE.connectedTextures(behaviour))
			.addLayer(() -> RenderType::getTranslucent)
			.initialProperties(() -> Blocks.GLASS)
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
			.item()
			.model((c, p) -> p.cubeColumn(c.getName(), p.modLoc(palettesDir() + c.getName()),
				p.modLoc("block/palettes/framed_glass")))
			.build()
			.register();
	}

	public static BlockEntry<ConnectedGlassPaneBlock> framedGlassPane(String name, AllCTs ctshift) {
		ResourceLocation sideTexture = Create.asResource(palettesDir() + "framed_glass");
		ResourceLocation itemSideTexture = Create.asResource(palettesDir() + name);
		ResourceLocation topTexture = Create.asResource(palettesDir() + "framed_glass_pane_top");
		Supplier<Supplier<RenderType>> renderType = () -> RenderType::getTranslucent;
		return connectedGlassPane(name, ctshift, sideTexture, itemSideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> customWindowPane(String name, AllCTs ctshift,
		Supplier<Supplier<RenderType>> renderType) {
		ResourceLocation topTexture = Create.asResource(palettesDir() + name + "_pane_top");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, ctshift, sideTexture, sideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(String woodType, AllCTs ctshift) {
		return woodenWindowPane(woodType, ctshift, () -> RenderType::getCutoutMipped);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(String woodType, AllCTs ctshift,
		Supplier<Supplier<RenderType>> renderType) {
		String name = woodType + "_window";
		ResourceLocation topTexture = new ResourceLocation("block/" + woodType + "_planks");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, ctshift, sideTexture, sideTexture, topTexture, renderType);
	}

	public static BlockEntry<GlassPaneBlock> standardGlassPane(String name, ResourceLocation sideTexture,
		ResourceLocation topTexture, Supplier<Supplier<RenderType>> renderType) {
		NonNullBiConsumer<DataGenContext<Block, GlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), sideTexture, topTexture);
		NonNullUnaryOperator<BlockBuilder<GlassPaneBlock, PalettesRegistrate>> connectedTextures = b -> b;
		return glassPane(name, sideTexture, topTexture, GlassPaneBlock::new, renderType, connectedTextures,
			stateProvider);
	}

	private static BlockEntry<ConnectedGlassPaneBlock> connectedGlassPane(String name, AllCTs ctshift,
		ResourceLocation sideTexture, ResourceLocation itemSideTexture, ResourceLocation topTexture,
		Supplier<Supplier<RenderType>> renderType) {
		NonNullUnaryOperator<BlockBuilder<ConnectedGlassPaneBlock, PalettesRegistrate>> connectedTextures =
			REGISTRATE.connectedTextures(new GlassPaneCTBehaviour(ctshift.get()));
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

		return glassPane(name, itemSideTexture, topTexture, ConnectedGlassPaneBlock::new, renderType, connectedTextures,
			stateProvider);
	}

	private static Function<RegistrateBlockstateProvider, ModelFile> getPaneModelProvider(String CGPparents,
		String prefix, String partial, ResourceLocation sideTexture, ResourceLocation topTexture) {
		return p -> p.models()
			.withExistingParent(prefix + partial, Create.asResource(CGPparents + partial))
			.texture("pane", sideTexture)
			.texture("edge", topTexture);
	}

	private static <G extends GlassPaneBlock> BlockEntry<G> glassPane(String name, ResourceLocation sideTexture,
		ResourceLocation topTexture, NonNullFunction<Properties, G> factory, Supplier<Supplier<RenderType>> renderType,
		NonNullUnaryOperator<BlockBuilder<G, PalettesRegistrate>> connectedTextures,
		NonNullBiConsumer<DataGenContext<Block, G>, RegistrateBlockstateProvider> stateProvider) {
		name += "_pane";

		return REGISTRATE.block(name, factory)
			.transform(connectedTextures)
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS_PANE)
			.blockstate(stateProvider)
			.item()
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
