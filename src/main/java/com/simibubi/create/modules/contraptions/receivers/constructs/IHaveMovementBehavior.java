package com.simibubi.create.modules.contraptions.receivers.constructs;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHaveMovementBehavior {

	public boolean visitPosition(World world, BlockPos pos, BlockState block, Direction movement, MechanicalPistonTileEntity piston);
	
}
