
package com.simibubi.create.foundation.utility.data;

import java.util.function.Function;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStateGen {

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> axisBlockProvider(
			boolean customItem) {
		return (c, p) -> BlockStateGen.axisBlock(c, p,
				$ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p));
	}

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

}
