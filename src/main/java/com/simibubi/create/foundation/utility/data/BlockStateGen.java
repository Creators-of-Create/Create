
package com.simibubi.create.foundation.utility.data;

import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.block.Block;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

public class BlockStateGen {

	/**
	 * Equivalent to BlockStateProvider#axisBlock without the need for a
	 * RotatedPillarBlock instance
	 */
	public static <T extends RotatedPillarKineticBlock> VariantBlockStateBuilder axisKineticBlock(
			DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, ExistingModelFile model) {
		RotatedPillarKineticBlock block = ctx.getEntry();
		return prov.getVariantBuilder(block).partialState().with(RotatedPillarBlock.AXIS, Axis.Y).modelForState()
				.modelFile(model).addModel().partialState().with(RotatedPillarBlock.AXIS, Axis.Z).modelForState()
				.modelFile(model).rotationX(90).addModel().partialState().with(RotatedPillarBlock.AXIS, Axis.X)
				.modelForState().modelFile(model).rotationX(90).rotationY(90).addModel();
	}

	/**
	 * Custom block models packaged with other partials. Example:
	 * models/block/schematicannon/block.json
	 */
	public static ExistingModelFile partialBaseModel(@NonnullType DataGenContext<?, ?> ctx,
			@NonnullType RegistrateBlockstateProvider prov) {
		return prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block"));
	}

	/**
	 * Custom block model from models/block/x.json
	 */
	public static ExistingModelFile standardModel(@NonnullType DataGenContext<?, ?> ctx,
			@NonnullType RegistrateBlockstateProvider prov) {
		return prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName()));
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/x/item.json
	 */
	public static ItemModelBuilder customItemModel(@NonnullType DataGenContext<Item, ? extends BlockItem> ctx,
			@NonnullType RegistrateItemModelProvider prov) {
		return prov.blockItem(() -> ctx.getEntry().getBlock(), "/item");
	}

}
