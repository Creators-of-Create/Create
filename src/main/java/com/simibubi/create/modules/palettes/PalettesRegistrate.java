package com.simibubi.create.modules.palettes;

import java.util.function.Supplier;

import com.simibubi.create.AllCTs;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.registrate.CreateRegistrateBase;
import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class PalettesRegistrate extends CreateRegistrateBase<PalettesRegistrate> {

	protected PalettesRegistrate(String modid) {
		super(modid, () -> Create.palettesCreativeTab);
	}

	public static NonNullLazyValue<PalettesRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(() -> new PalettesRegistrate(modid));
	}

	public <T extends Block> BlockBuilder<T, PalettesRegistrate> baseBlock(String name,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom) {
		return super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.simpleItem();
	}

	@Override
	public Sections currentSection() {
		return Sections.PALETTES;
	}

	// Specific patterns

	public BlockEntry<WindowBlock> woodenWindowBlock(String woodType, AllCTs ct) {
		return woodenWindowBlock(woodType, ct, () -> RenderType::getCutoutMipped);
	}

	public BlockEntry<WindowBlock> customWindowBlock(String name, AllCTs ct,
		Supplier<Supplier<RenderType>> renderType) {
		NonNullFunction<String, ResourceLocation> end_texture =
			n -> Create.asResource("block/palettes/" + name + "_end");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource("block/palettes/" + n);
		return windowBlock(name, ct, renderType, end_texture, side_texture);
	}

	public BlockEntry<WindowBlock> woodenWindowBlock(String woodType, AllCTs ct,
		Supplier<Supplier<RenderType>> renderType) {
		String name = woodType + "_window";
		NonNullFunction<String, ResourceLocation> end_texture =
			$ -> new ResourceLocation("block/" + woodType + "_planks");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource("block/palettes/" + n);
		return windowBlock(name, ct, renderType, end_texture, side_texture);
	}

	public BlockEntry<WindowBlock> windowBlock(String name, AllCTs ct, Supplier<Supplier<RenderType>> renderType,
		NonNullFunction<String, ResourceLocation> endTexture, NonNullFunction<String, ResourceLocation> sideTexture) {
		return block(name, WindowBlock::new)
			.transform(connectedTextures(new HorizontalCTBehaviour(ct.get())))
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), sideTexture.apply(c.getName()), endTexture.apply(c.getName()))))
			.simpleItem()
			.register();
	}

	public BlockEntry<ConnectedGlassBlock> framedGlass(String name, ConnectedTextureBehaviour behaviour) {
		return block(name, ConnectedGlassBlock::new)
			.transform(connectedTextures(behaviour))
			.addLayer(() -> RenderType::getTranslucent)
			.initialProperties(() -> Blocks.GLASS)
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
			.item()
			.model((c, p) -> p.cubeColumn(c.getName(), p.modLoc("block/palettes/" + c.getName()),
				p.modLoc("block/palettes/framed_glass")))
			.build()
				.register();
	}

	private <T extends Block> NonNullUnaryOperator<BlockBuilder<T, PalettesRegistrate>> connectedTextures(
			ConnectedTextureBehaviour behavior) {
		return b -> b.onRegister(entry -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
				CreateClient.getCustomBlockModels()
						.register(entry.delegate, model -> new CTModel(model, behavior))));
	}
}
