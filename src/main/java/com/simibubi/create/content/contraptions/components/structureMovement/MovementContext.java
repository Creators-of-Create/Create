package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.UnaryOperator;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.common.util.Constants.NBT;

public class MovementContext {

	public Vector3d position;
	public Vector3d motion;
	public Vector3d relativeMotion;
	public UnaryOperator<Vector3d> rotation;

	public World world;
	public BlockState state;
	public BlockPos localPos;
	public CompoundNBT tileData;

	public boolean stall;
	public boolean firstMovement;
	public CompoundNBT data;
	public Contraption contraption;
	public Object temporaryData;

	public MovementContext(World world, BlockInfo info) {
		this.world = world;
		this.state = info.state;
		this.tileData = info.nbt;
		localPos = info.pos;

		firstMovement = true;
		motion = Vector3d.ZERO;
		relativeMotion = Vector3d.ZERO;
		rotation = v -> v;
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

	public static MovementContext readNBT(World world, BlockInfo info, CompoundNBT nbt) {
		MovementContext context = new MovementContext(world, info);
		context.motion = VecHelper.readNBT(nbt.getList("Motion", NBT.TAG_DOUBLE));
		context.relativeMotion = VecHelper.readNBT(nbt.getList("RelativeMotion", NBT.TAG_DOUBLE));
		if (nbt.contains("Position"))
			context.position = VecHelper.readNBT(nbt.getList("Position", NBT.TAG_DOUBLE));
		context.stall = nbt.getBoolean("Stall");
		context.firstMovement = nbt.getBoolean("FirstMovement");
		context.data = nbt.getCompound("Data");
		return context;
	}

	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt.put("Motion", VecHelper.writeNBT(motion));
		nbt.put("RelativeMotion", VecHelper.writeNBT(relativeMotion));
		if (position != null)
			nbt.put("Position", VecHelper.writeNBT(position));
		nbt.putBoolean("Stall", stall);
		nbt.putBoolean("FirstMovement", firstMovement);
		nbt.put("Data", data);
		return nbt;
	}

}