package com.simibubi.create.foundation.data;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class ModelGen {

	public static ModelFile createOvergrown(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov,
		ResourceLocation block, ResourceLocation overlay) {
		return createOvergrown(ctx, prov, block, block, block, overlay);
	}

	public static ModelFile createOvergrown(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov,
		ResourceLocation side, ResourceLocation top, ResourceLocation bottom, ResourceLocation overlay) {
		return prov.models()
			.withExistingParent(ctx.getName(), Create.asResource("block/overgrown"))
			.texture("particle", side)
			.texture("side", side)
			.texture("top", top)
			.texture("bottom", bottom)
			.texture("overlay", overlay);
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel() {
		return b -> b.model(AssetLookup::customItemModel)
			.build();
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel(String... path) {
		return b -> b.model(AssetLookup.customBlockItemModel(path))
			.build();
	}

}
