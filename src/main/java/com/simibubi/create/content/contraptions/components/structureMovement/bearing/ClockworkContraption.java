package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClockworkContraption extends Contraption {

	protected Direction facing;
	public HandType handType;
	public int offset;
	private Set<BlockPos> ignoreBlocks = new HashSet<>();

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.CLOCKWORK;
	}

	private void ignoreBlocks(Set<BlockPos> blocks, BlockPos anchor) {
		for (BlockPos blockPos : blocks)
			ignoreBlocks.add(anchor.add(blockPos));
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.offset(facing.getOpposite(), offset + 1));
	}

	public static Pair<ClockworkContraption, ClockworkContraption> assembleClockworkAt(World world, BlockPos pos,
			Direction direction) {
		if (isFrozen())
			return null;

		int hourArmBlocks = 0;

		ClockworkContraption hourArm = new ClockworkContraption();
		ClockworkContraption minuteArm = null;

		hourArm.facing = direction;
		hourArm.handType = HandType.HOUR;
		if (!hourArm.searchMovedStructure(world, pos, direction))
			return null;
		for (int i = 0; i < 16; i++) {
			BlockPos offsetPos = BlockPos.ZERO.offset(direction, i);
			if (hourArm.blocks.containsKey(offsetPos))
				continue;
			hourArmBlocks = i;
			break;
		}

		if (hourArmBlocks > 0) {
			minuteArm = new ClockworkContraption();
			minuteArm.facing = direction;
			minuteArm.handType = HandType.MINUTE;
			minuteArm.offset = hourArmBlocks;
			minuteArm.ignoreBlocks(hourArm.blocks.keySet(), hourArm.anchor);
			if (!minuteArm.searchMovedStructure(world, pos, direction))
				return null;
			if (minuteArm.blocks.isEmpty())
				minuteArm = null;
		}

		hourArm.initActors(world);
		hourArm.expandBoundsAroundAxis(direction.getAxis());
		if (minuteArm != null) {
			minuteArm.initActors(world);
			minuteArm.expandBoundsAroundAxis(direction.getAxis());
		}
		return Pair.of(hourArm, minuteArm);
	}

	@Override
	public boolean searchMovedStructure(World world, BlockPos pos, Direction direction) {
		return super.searchMovedStructure(world, pos.offset(direction, offset + 1), null);
	}

	@Override
	protected boolean moveBlock(World world, BlockPos pos, Direction direction, List<BlockPos> frontier,
			Set<BlockPos> visited) {
		if (ignoreBlocks.contains(pos))
			return true;
		return super.moveBlock(world, pos, direction, frontier, visited);
	}

	@Override
	public CompoundNBT writeNBT() {
		CompoundNBT tag = super.writeNBT();
		tag.putInt("facing", facing.getIndex());
		tag.putInt("offset", offset);
		NBTHelper.writeEnum(tag, "HandType", handType);
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT tag) {
		facing = Direction.byIndex(tag.getInt("Facing"));
		handType = NBTHelper.readEnum(tag, "HandType", HandType.class);
		offset = tag.getInt("offset");
		super.readNBT(world, tag);
	}

	public static enum HandType {
		HOUR, MINUTE
	}

}
