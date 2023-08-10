package com.simibubi.create.content.contraptions.behaviour;

import java.util.function.UnaryOperator;

import com.simibubi.create.content.contraptions.Contraption;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

public class MovementContext {

	public Vec3 position;
	public Vec3 motion;
	public Vec3 relativeMotion;
	public UnaryOperator<Vec3> rotation;

	public Level world;
	public BlockState state;
	public BlockPos localPos;
	public CompoundTag blockEntityData;

	public boolean stall;
	public boolean disabled;
	public boolean firstMovement;
	public CompoundTag data;
	public Contraption contraption;
	public Object temporaryData;

	public MovementContext(Level world, StructureBlockInfo info, Contraption contraption) {
		this.world = world;
		this.state = info.state;
		this.blockEntityData = info.nbt;
		this.contraption = contraption;
		localPos = info.pos;

		disabled = false;
		firstMovement = true;
		motion = Vec3.ZERO;
		relativeMotion = Vec3.ZERO;
		rotation = v -> v;
		position = null;
		data = new CompoundTag();
		stall = false;
	}

	public float getAnimationSpeed() {
		int modifier = 1000;
		double length = -motion.length();
		if (disabled)
			return 0;
		if (world.isClientSide && contraption.stalled)
			return 700;
		if (Math.abs(length) < 1 / 512f)
			return 0;
		return (((int) (length * modifier + 100 * Math.signum(length))) / 100) * 100;
	}

	public static MovementContext readNBT(Level world, StructureBlockInfo info, CompoundTag nbt, Contraption contraption) {
		MovementContext context = new MovementContext(world, info, contraption);
		context.motion = VecHelper.readNBT(nbt.getList("Motion", Tag.TAG_DOUBLE));
		context.relativeMotion = VecHelper.readNBT(nbt.getList("RelativeMotion", Tag.TAG_DOUBLE));
		if (nbt.contains("Position"))
			context.position = VecHelper.readNBT(nbt.getList("Position", Tag.TAG_DOUBLE));
		context.stall = nbt.getBoolean("Stall");
		context.firstMovement = nbt.getBoolean("FirstMovement");
		context.data = nbt.getCompound("Data");
		return context;
	}

	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.put("Motion", VecHelper.writeNBT(motion));
		nbt.put("RelativeMotion", VecHelper.writeNBT(relativeMotion));
		if (position != null)
			nbt.put("Position", VecHelper.writeNBT(position));
		nbt.putBoolean("Stall", stall);
		nbt.putBoolean("FirstMovement", firstMovement);
		nbt.put("Data", data.copy());
		return nbt;
	}

}
