package com.simibubi.create.modules.schematics.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LaunchedBlock {

	private final SchematicannonTileEntity te;
	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public BlockState state;

	public LaunchedBlock(SchematicannonTileEntity schematicannonTileEntity, BlockPos target, BlockState state) {
		te = schematicannonTileEntity;
		this.target = target;
		this.state = state;
		totalTicks = (int) (Math.max(10, MathHelper.sqrt(MathHelper.sqrt(target.distanceSq(te.getPos()))) * 4f));
		ticksRemaining = totalTicks;
	}

	public LaunchedBlock(SchematicannonTileEntity schematicannonTileEntity, BlockPos target, BlockState state,
			int ticksLeft, int total) {
		te = schematicannonTileEntity;
		this.target = target;
		this.state = state;
		this.totalTicks = total;
		this.ticksRemaining = ticksLeft;
	}

	public void update() {
		if (ticksRemaining > 0)
			ticksRemaining--;
	}
}