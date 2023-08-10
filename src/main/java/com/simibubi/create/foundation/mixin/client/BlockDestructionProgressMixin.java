package com.simibubi.create.foundation.mixin.client;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.foundation.block.render.BlockDestructionProgressExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(BlockDestructionProgress.class)
public class BlockDestructionProgressMixin implements BlockDestructionProgressExtension {
	@Unique
	private Set<BlockPos> create$extraPositions;

	@Override
	public Set<BlockPos> getExtraPositions() {
		return create$extraPositions;
	}

	@Override
	public void setExtraPositions(Set<BlockPos> positions) {
		create$extraPositions = positions;
	}
}
