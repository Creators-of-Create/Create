package com.simibubi.create.content.contraptions.bearing;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClockworkContraption extends Contraption {

	protected Direction facing;
	public HandType handType;
	public int offset;
	private Set<BlockPos> ignoreBlocks = new HashSet<>();

	@Override
	public ContraptionType getType() {
		return ContraptionType.CLOCKWORK;
	}

	private void ignoreBlocks(Set<BlockPos> blocks, BlockPos anchor) {
		for (BlockPos blockPos : blocks)
			ignoreBlocks.add(anchor.offset(blockPos));
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.relative(facing.getOpposite(), offset + 1));
	}

	public static Pair<ClockworkContraption, ClockworkContraption> assembleClockworkAt(Level world, BlockPos pos,
		Direction direction) throws AssemblyException {
		int hourArmBlocks = 0;

		ClockworkContraption hourArm = new ClockworkContraption();
		ClockworkContraption minuteArm = null;

		hourArm.facing = direction;
		hourArm.handType = HandType.HOUR;
		if (!hourArm.assemble(world, pos))
			return null;
		for (int i = 0; i < 16; i++) {
			BlockPos offsetPos = BlockPos.ZERO.relative(direction, i);
			if (hourArm.getBlocks()
				.containsKey(offsetPos))
				continue;
			hourArmBlocks = i;
			break;
		}

		if (hourArmBlocks > 0) {
			minuteArm = new ClockworkContraption();
			minuteArm.facing = direction;
			minuteArm.handType = HandType.MINUTE;
			minuteArm.offset = hourArmBlocks;
			minuteArm.ignoreBlocks(hourArm.getBlocks()
				.keySet(), hourArm.anchor);
			if (!minuteArm.assemble(world, pos))
				return null;
			if (minuteArm.getBlocks()
				.isEmpty())
				minuteArm = null;
		}

		hourArm.startMoving(world);
		hourArm.expandBoundsAroundAxis(direction.getAxis());
		if (minuteArm != null) {
			minuteArm.startMoving(world);
			minuteArm.expandBoundsAroundAxis(direction.getAxis());
		}
		return Pair.of(hourArm, minuteArm);
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		return searchMovedStructure(world, pos, facing);
	}

	@Override
	public boolean searchMovedStructure(Level world, BlockPos pos, Direction direction) throws AssemblyException {
		return super.searchMovedStructure(world, pos.relative(direction, offset + 1), null);
	}

	@Override
	protected boolean moveBlock(Level world, Direction direction, Queue<BlockPos> frontier,
		Set<BlockPos> visited) throws AssemblyException {
		if (ignoreBlocks.contains(frontier.peek())) {
			frontier.poll();
			return true;
		}
		return super.moveBlock(world, direction, frontier, visited);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("facing", facing.get3DDataValue());
		tag.putInt("offset", offset);
		NBTHelper.writeEnum(tag, "HandType", handType);
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag tag, boolean spawnData) {
		facing = Direction.from3DDataValue(tag.getInt("facing"));
		handType = NBTHelper.readEnum(tag, "HandType", HandType.class);
		offset = tag.getInt("offset");
		super.readNBT(world, tag, spawnData);
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		if (BlockPos.ZERO.equals(localPos) || BlockPos.ZERO.equals(localPos.relative(facing)))
			return false;
		return facing.getAxis() == this.facing.getAxis();
	}

	public static enum HandType {
		HOUR, MINUTE
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new AnchoredLighter(this);
	}
}
