package com.simibubi.create.modules.logistics.block.extractor;

import com.simibubi.create.foundation.utility.data.AssetLookup;
import com.simibubi.create.foundation.utility.data.SpecialBlockStateGen;
import com.simibubi.create.modules.logistics.block.funnel.FunnelBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class VerticalExtractorGenerator extends SpecialBlockStateGen {

	private boolean linked;

	public VerticalExtractorGenerator(boolean linked) {
		this.linked = linked;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(ExtractorBlock.Vertical.UPWARD) ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return (state.get(FunnelBlock.UPWARD) ? 0 : 180) + horizontalAngle(state.get(FunnelBlock.HORIZONTAL_FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return AssetLookup.forPowered(ctx, prov, "extractor/vertical" + (linked ? "_linked" : ""))
			.apply(state);
	}

}
