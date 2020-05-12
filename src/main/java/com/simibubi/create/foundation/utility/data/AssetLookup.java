package com.simibubi.create.foundation.utility.data;

import java.util.function.Function;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

public class AssetLookup {

	/**
	 * Custom block models packaged with other partials. Example:
	 * models/block/schematicannon/block.json <br>
	 * <br>
	 * Adding "powered", "vertical" will look for /block_powered_vertical.json
	 */
	public static ModelFile partialBaseModel(DataGenContext<?, ?> ctx, RegistrateBlockstateProvider prov,
			String... suffix) {
		String string = "/block";
		for (String suf : suffix)
			string += "_" + suf;
		final String location = "block/" + ctx.getName() + string;
		return prov.models()
				.getExistingFile(prov.modLoc(location));
	}

	/**
	 * Custom block model from models/block/x.json
	 */
	public static ModelFile standardModel(DataGenContext<?, ?> ctx, RegistrateBlockstateProvider prov) {
		return prov.models()
				.getExistingFile(prov.modLoc("block/" + ctx.getName()));
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/x/item.json
	 */
	public static ItemModelBuilder customItemModel(DataGenContext<Item, ? extends BlockItem> ctx,
			RegistrateItemModelProvider prov) {
		return prov.blockItem(() -> ctx.getEntry()
				.getBlock(), "/item");
	}

	public static Function<BlockState, ModelFile> forPowered(DataGenContext<?, ?> ctx,
			RegistrateBlockstateProvider prov) {
		return state -> state.get(BlockStateProperties.POWERED) ? partialBaseModel(ctx, prov, "powered")
				: partialBaseModel(ctx, prov);
	}

}
