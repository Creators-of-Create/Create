package com.simibubi.create.modules.contraptions.base;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRotate {

	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face);
	public boolean isGearTowards(World world, BlockPos pos, BlockState state, Direction face);
	
	public Axis getRotationAxis(BlockState state);
	
}
