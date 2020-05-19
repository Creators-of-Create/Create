
package com.simibubi.create.foundation.utility.data;

import java.util.function.Function;

import com.simibubi.create.foundation.world.OxidizingBlock;
import com.simibubi.create.modules.palettes.PavedBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStateGen {

	// Functions

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> axisBlockProvider(
		boolean customItem) {
		return (c, p) -> BlockStateGen.axisBlock(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProvider(
		boolean customItem) {
		return (c, p) -> p.directionalBlock(c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalWheelProvider(
		boolean customItem) {
		return (c, p) -> BlockStateGen.horizontalWheel(c, p, getBlockModel(customItem, c, p));
	}

	public static <P> NonNullUnaryOperator<BlockBuilder<OxidizingBlock, P>> oxidizedBlockstate() {
		return b -> b.blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				String name = ModelGen.getOxidizedModel(ctx.getName(), state.get(OxidizingBlock.OXIDIZATION));
				return ConfiguredModel.builder()
					.modelFile(prov.models()
						.cubeAll(name, prov.modLoc(name)))
					.build();
			}));
	}

	// Utility

	private static <T extends Block> Function<BlockState, ModelFile> getBlockModel(boolean customItem,
		DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
		return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
	}

	// Generators

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				Axis axis = state.get(BlockStateProperties.AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationX(axis == Axis.Y ? 0 : 90)
					.rotationY(axis == Axis.X ? 90 : 0)
					.build();
			});
	}

	public static <T extends Block> void horizontalWheel(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.get())
			.forAllStates(state -> ConfiguredModel.builder()
				.modelFile(modelFunc.apply(state))
				.rotationX(90)
				.rotationY(((int) state.get(BlockStateProperties.HORIZONTAL_FACING)
					.getHorizontalAngle() + 180) % 360)
				.build());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		String textureSubDir) {
		cubeAll(ctx, prov, textureSubDir, ctx.getName());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		String textureSubDir, String name) {
		String texturePath = "block/" + textureSubDir + name;
		prov.simpleBlock(ctx.get(), prov.models()
			.cubeAll(ctx.getName(), prov.modLoc(texturePath)));
	}

	public static <T extends Block> void pavedBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		ModelFile top, ModelFile covered) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> ConfiguredModel.builder()
				.modelFile(state.get(PavedBlock.COVERED) ? covered : top)
				.build());
	}

}
