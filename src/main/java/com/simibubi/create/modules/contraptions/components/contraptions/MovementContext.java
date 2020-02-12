package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class MovementContext {

	public Vec3d position;
	public Vec3d motion;
	public Vec3d relativeMotion;
	public Vec3d rotation;
	public World world;
	public BlockState state;

	public boolean stall;
	public CompoundNBT data;
	public Contraption contraption;

	public MovementContext(World world, BlockState state) {
		this.world = world;
		this.state = state;

		motion = Vec3d.ZERO;
		relativeMotion = Vec3d.ZERO;
		rotation = Vec3d.ZERO;
		position = null;
		data = new CompoundNBT();
		stall = false;
	}

	public float getAnimationSpeed() {
		int modifier = 1000;
		double length = -motion.length();
		if (world.isRemote && contraption.stalled)
			return 700;
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
		context.stall = nbt.getBoolean("Stall");
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
		nbt.putBoolean("Stall", stall);
		nbt.put("Data", data);
		return nbt;
	}

}