package com.simibubi.create.foundation.block.render;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public interface BlockDestructionProgressExtension {
	@Nullable
	Set<BlockPos> create$getExtraPositions();

	void create$setExtraPositions(@Nullable Set<BlockPos> positions);
}
