package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class StabilizedContraption extends Contraption {

	public UUID parentID;

	private Direction facing;

	public StabilizedContraption() {}

	public StabilizedContraption(UUID parentID, Direction facing) {
		this.parentID = parentID;
		this.facing = facing;
	}

	@Override
	public boolean assemble(World world, BlockPos pos) {
		BlockPos offset = pos.offset(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		expandBoundsAroundAxis(Axis.Y);
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.STABILIZED;
	}
	
	@Override
	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT tag = super.writeNBT(spawnPacket);
		tag.putInt("Facing", facing.getIndex());
		tag.putUniqueId("Parent", parentID);
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT tag, boolean spawnData) {
		facing = Direction.byIndex(tag.getInt("Facing"));
		parentID = tag.getUniqueId("Parent");
		super.readNBT(world, tag, spawnData);
	}
	
	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return false;
	}
	
	public Direction getFacing() {
		return facing;
	}

	@Override
	public ContraptionLighter<?> makeLighter() {
		return new StabilizedLighter(this);
	}
}
