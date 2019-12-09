package com.simibubi.create.modules.contraptions.receivers.contraptions;

import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public interface IHaveMovementBehavior {

	public class MovementContext {

		public Vec3d position;
		public Vec3d motion;
		public Vec3d relativeMotion;
		public Vec3d rotation;
		public World world;
		public BlockState state;

		public float movementSpeedModifier;
		public CompoundNBT data;

		public MovementContext(World world, BlockState state) {
			this.world = world;
			this.state = state;

			motion = Vec3d.ZERO;
			relativeMotion = Vec3d.ZERO;
			rotation = Vec3d.ZERO;
			position = null;
			data = new CompoundNBT();
			movementSpeedModifier = 1;
		}

		public float getAnimationSpeed() {
			int modifier = 1000;
			double length = -motion.length();
			if (Math.abs(length) < 1 / 512f)
				return 0;
			return (((int) (length * modifier + 100 * Math.signum(length))) / 100) * 100;
		}

		public static MovementContext readNBT(World world, CompoundNBT nbt) {
			BlockState state = NBTUtil.readBlockState(nbt.getCompound("State"));
			MovementContext context = new MovementContext(world, state);
			context.motion = VecHelper.readNBT(nbt.getList("Motion", NBT.TAG_DOUBLE));
			context.relativeMotion = VecHelper.readNBT(nbt.getList("RelativeMotion", NBT.TAG_DOUBLE));
			context.rotation = VecHelper.readNBT(nbt.getList("Rotation", NBT.TAG_DOUBLE));
			if (nbt.contains("Position"))
				context.position = VecHelper.readNBT(nbt.getList("Position", NBT.TAG_DOUBLE));
			context.movementSpeedModifier = nbt.getFloat("SpeedModifier");
			context.data = nbt.getCompound("Data");
			return context;
		}

		public CompoundNBT writeToNBT(CompoundNBT nbt) {
			nbt.put("State", NBTUtil.writeBlockState(state));
			nbt.put("Motion", VecHelper.writeNBT(motion));
			nbt.put("RelativeMotion", VecHelper.writeNBT(relativeMotion));
			nbt.put("Rotation", VecHelper.writeNBT(rotation));
			if (position != null)
				nbt.put("Position", VecHelper.writeNBT(position));
			nbt.putFloat("SpeedModifier", movementSpeedModifier);
			nbt.put("Data", data);
			return nbt;
		}

	}

	default boolean isActive(MovementContext context) {
		return true;
	}

	default void tick(MovementContext context) {
	}

	default void startMoving(MovementContext context) {
	}

	default void visitNewPosition(MovementContext context, BlockPos pos) {
	}

	default Vec3d getActiveAreaOffset(MovementContext context) {
		return Vec3d.ZERO;
	}

	@OnlyIn(value = Dist.CLIENT)
	default SuperByteBuffer renderInContraption(MovementContext context) {
		return null;
	}

}
