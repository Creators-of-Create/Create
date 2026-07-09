package com.simibubi.create.content.redstone.thresholdSwitch;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.createmod.catnip.api.lang.Lang;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.tterrag.registrate.providers.generators.ModelFile;

import java.util.HashMap;
import java.util.Map;

public class ThresholdSwitchGenerator extends SpecialBlockStateGen {

	private final Map<String, ModelFile> models = new HashMap<>();

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(ThresholdSwitchBlock.FACING)) + 180;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		int level = state.getValue(ThresholdSwitchBlock.LEVEL);
		String path = "block/threshold_switch/block_" + Lang.asId(state.getValue(ThresholdSwitchBlock.TARGET)
			.name());
		String modelName = path + "_" + level;
		return models.computeIfAbsent(modelName, $ -> prov.models()
			.withExistingParent(modelName, Create.asResource(path))
			.texture("level", Create.asResource("block/threshold_switch/level_" + level)));
	}

}
