package com.simibubi.create.foundation.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.foundation.block.render.BlockDestructionProgressExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(BlockDestructionProgress.class)
public class BlockDestructionProgressMixin implements BlockDestructionProgressExtension {
	@Unique
	private Set<BlockPos> extraPositions;

	@Override
	public Set<BlockPos> getExtraPositions() {
		return extraPositions;
	}

	@Override
	public void setExtraPositions(Set<BlockPos> positions) {
		extraPositions = positions;
	}
}
