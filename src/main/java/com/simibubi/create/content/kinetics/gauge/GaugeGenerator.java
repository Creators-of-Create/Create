package com.simibubi.create.content.kinetics.gauge;

import com.simibubi.create.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GaugeGenerator extends DirectionalAxisBlockStateGen {

	@Override
	public <T extends Block> String getModelPrefix(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		return "block/gauge/base";
	}

}
