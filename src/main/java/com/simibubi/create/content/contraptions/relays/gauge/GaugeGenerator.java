package com.simibubi.create.content.contraptions.relays.gauge;

import com.simibubi.create.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class GaugeGenerator extends DirectionalAxisBlockStateGen {

	@Override
	public <T extends Block> String getModelPrefix(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return "block/gauge/base";
	}

}
