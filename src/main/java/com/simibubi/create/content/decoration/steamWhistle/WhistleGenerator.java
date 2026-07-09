package com.simibubi.create.content.decoration.steamWhistle;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.tterrag.registrate.providers.generators.ModelFile;

public class WhistleGenerator extends SpecialBlockStateGen {

	private final Map<Identifier, ModelFile> poweredModels = new HashMap<>();

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(WhistleBlock.FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		String wall = state.getValue(WhistleBlock.WALL) ? "wall" : "floor";
		String size = state.getValue(WhistleBlock.SIZE)
			.getSerializedName();
		boolean powered = state.getValue(WhistleBlock.POWERED);
		ModelFile model = AssetLookup.partialBaseModel(ctx, prov, size, wall);
		if (!powered)
			return model;
		Identifier parentLocation = model.getLocation();
		return poweredModels.computeIfAbsent(parentLocation, location -> prov.models()
			.withExistingParent(location.getPath() + "_powered", location)
			.texture("2", Create.asResource("block/copper_redstone_plate_powered")));
	}

}
