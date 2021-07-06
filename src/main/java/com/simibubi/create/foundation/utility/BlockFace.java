package com.simibubi.create.foundation.utility;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BlockFace extends Pair<BlockPos, Direction> {

	public BlockFace(BlockPos first, Direction second) {
		super(first, second);
	}

	public boolean isEquivalent(BlockFace other) {
		if (equals(other))
			return true;
		return getConnectedPos().equals(other.getPos()) && getPos().equals(other.getConnectedPos());
	}

	public BlockPos getPos() {
		return getFirst();
	}

	public Direction getFace() {
		return getSecond();
	}

	public Direction getOppositeFace() {
		return getSecond().getOpposite();
	}

	public BlockFace getOpposite() {
		return new BlockFace(getConnectedPos(), getOppositeFace());
	}

	public BlockPos getConnectedPos() {
		return getPos().offset(getFace());
	}

	public CompoundNBT serializeNBT() {
		CompoundNBT compoundNBT = new CompoundNBT();
		compoundNBT.put("Pos", NBTUtil.writeBlockPos(getPos()));
		NBTHelper.writeEnum(compoundNBT, "Face", getFace());
		return compoundNBT;
	}

	public static BlockFace fromNBT(CompoundNBT compound) {
		return new BlockFace(NBTUtil.readBlockPos(compound.getCompound("Pos")),
			NBTHelper.readEnum(compound, "Face", Direction.class));
	}

}
