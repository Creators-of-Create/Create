package com.simibubi.create.foundation.utility.ghost;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GhostBlockParams {

	protected final BlockState state;
	protected BlockPos pos;
	protected Supplier<Float> alphaSupplier;

	private GhostBlockParams(BlockState state) {
		this.state = state;
		this.pos = BlockPos.ZERO;
		this.alphaSupplier = () -> 1f;
	}

	public static GhostBlockParams of(BlockState state) {
		return new GhostBlockParams(state);
	}

	public static GhostBlockParams of(Block block) {
		return of(block.defaultBlockState());
	}

	public GhostBlockParams at(BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public GhostBlockParams at(int x, int y, int z) {
		return this.at(new BlockPos(x, y, z));
	}

	public GhostBlockParams alpha(Supplier<Float> alphaSupplier) {
		this.alphaSupplier = alphaSupplier;
		return this;
	}

	public GhostBlockParams alpha(float alpha) {
		return this.alpha(() -> alpha);
	}

	public GhostBlockParams breathingAlpha() {
		return this.alpha(() -> (float) GhostBlocks.getBreathingAlpha());
	}
}
