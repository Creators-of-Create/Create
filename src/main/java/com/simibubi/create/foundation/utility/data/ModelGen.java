package com.simibubi.create.foundation.utility.data;

import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class ModelGen {

	public static ModelFile createOvergrown(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov,
			ResourceLocation block, ResourceLocation overlay) {
		return createOvergrown(ctx, prov, block, block, block, overlay);
	}

	public static ModelFile createOvergrown(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov,
			ResourceLocation side, ResourceLocation top, ResourceLocation bottom, ResourceLocation overlay) {
		return prov.models()
				.withExistingParent(ctx.getName(), new ResourceLocation(Create.ID, "block/overgrown"))
				.texture("particle", side)
				.texture("side", side)
				.texture("top", top)
				.texture("bottom", bottom)
				.texture("overlay", overlay);
	}

}
