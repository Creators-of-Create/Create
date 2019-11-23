package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

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
		public Vec3d motion;
		public float movementSpeedModifier = 1;

		public MoverType moverType;
		public World world;
		public BlockState state;

		public MovementContext(BlockState state, MoverType moverType) {
			this.state = state;
			this.moverType = moverType;
		}

		public Direction getMovementDirection() {
			return Direction.getFacingFromVector(motion.x, motion.y, motion.z);
		}

		public float getAnimationSpeed() {
			int modifier = moverType == MoverType.MINECART ? 1000 : 200;
			return ((int) (motion.length() * modifier)) / 100 * 100;
		}

		public static MovementContext readNBT(CompoundNBT nbt) {
			MovementContext context = new MovementContext(NBTUtil.readBlockState(nbt.getCompound("State")),
					MoverType.valueOf(nbt.getString("MoverType")));
			context.motion = VecHelper.readNBT(nbt.getList("Motion", NBT.TAG_DOUBLE));
			context.movementSpeedModifier = nbt.getFloat("SpeedModifier");
			context.currentGridPos = NBTUtil.readBlockPos(nbt.getCompound("GridPos"));
			return context;
		}

		public CompoundNBT writeToNBT(CompoundNBT nbt) {
			nbt.put("State", NBTUtil.writeBlockState(state));
			nbt.putString("MoverType", moverType.name());
			nbt.put("Motion", VecHelper.writeNBT(motion));
			nbt.putFloat("SpeedModifier", movementSpeedModifier);
			nbt.put("GridPos", NBTUtil.writeBlockPos(currentGridPos));
			return nbt;
		}

	}

	@OnlyIn(value = Dist.CLIENT)
	default SuperByteBuffer renderInContraption(MovementContext context) {
		return null;
	}

}
