package com.simibubi.create.content.contraptions;

import net.minecraft.world.level.block.state.BlockState;

public interface ITransformableBlock {
	BlockState transform(BlockState state, StructureTransform transform);
}
