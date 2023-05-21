package com.simibubi.create.content.decoration.bracket;

import com.simibubi.create.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class BracketGenerator extends DirectionalAxisBlockStateGen {

	private String material;

	public BracketGenerator(String material) {
		this.material = material;
	}

	@Override
	public <T extends Block> String getModelPrefix(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return "";
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String type = state.getValue(BracketBlock.TYPE)
			.getSerializedName();
		boolean vertical = state.getValue(BracketBlock.FACING)
			.getAxis()
			.isVertical();

		String path = "block/bracket/" + type + "/" + (vertical ? "ground" : "wall");

		return prov.models()
			.withExistingParent(path + "_" + material, prov.modLoc(path))
			.texture("bracket", prov.modLoc("block/bracket_" + material))
			.texture("plate", prov.modLoc("block/bracket_plate_" + material));
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> itemModel(String material) {
		return b -> b.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/bracket/item"))
			.texture("bracket", p.modLoc("block/bracket_" + material))
			.texture("plate", p.modLoc("block/bracket_plate_" + material)))
			.build();
	}

}
