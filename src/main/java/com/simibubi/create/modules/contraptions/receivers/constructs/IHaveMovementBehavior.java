package com.simibubi.create.modules.contraptions.receivers.constructs;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHaveMovementBehavior {

	default IMovementContext visitPosition(World world, BlockPos pos, BlockState block, Direction movement,
			MechanicalPistonTileEntity piston) {
		return IdleMovementContext.INSTANCE;
	}

	default void tick(MechanicalPistonTileEntity piston) {
	}

	default boolean hasSpecialRenderer() {
		return false;
	}

	public interface IMovementContext {
		default boolean isBlocking() {
			return false;
		}
	}

	public static class IdleMovementContext implements IMovementContext {
		public static IdleMovementContext INSTANCE = new IdleMovementContext();
	}

}
