package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public class SequencedGearshiftGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(SequencedGearshiftBlock.VERTICAL) ? 90 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return state.getValue(SequencedGearshiftBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String variant = "idle";
		int seq = state.getValue(SequencedGearshiftBlock.STATE);
		if (seq > 0)
			variant = "seq_" + seq;
		return prov.models()
			.getExistingFile(prov.modLoc("block/" + ctx.getName() + "/" + variant));
	}

}
