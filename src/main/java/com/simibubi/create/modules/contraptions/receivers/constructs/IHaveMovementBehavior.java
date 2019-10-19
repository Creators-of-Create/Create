package com.simibubi.create.modules.contraptions.receivers.constructs;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveMovementBehavior {

	public enum MoverType {
		PISTON, BEARING, MINECART;
	}

	default void visitPosition(MovementContext context) {
	}

	default void tick(MechanicalPistonTileEntity piston) {
	}

	default boolean hasSpecialRenderer() {
		return false;
	}

	public class MovementContext {

		public BlockPos currentGridPos;
		public Vec3d movementVec;
		public float movementSpeedModifier = 1;

		public MoverType moverType;
		public Object mover;
		public World world;
		public BlockState state;

		public MovementContext(World world, BlockState state, MoverType moverType, Object mover) {
			this.world = world;
			this.state = state;
			this.moverType = moverType;
			this.mover = mover;
		}

		public Direction getMovementDirection() {
			return Direction.getFacingFromVector(movementVec.x, movementVec.y, movementVec.z);
		}

	}

	@OnlyIn(value = Dist.CLIENT)
	default void renderInConstruct(MovementContext context, double x, double y, double z, BufferBuilder buffer) {
	}

}
