package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.NonStationaryLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.TranslatingContraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class GantryContraption extends TranslatingContraption {

	protected Direction facing;

	public GantryContraption() {}

	public GantryContraption(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean assemble(World world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		startMoving(world);
		return true;
	}

	@Override
	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT tag = super.writeNBT(spawnPacket);
		tag.putInt("Facing", facing.get3DDataValue());
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT tag, boolean spawnData) {
		facing = Direction.from3DDataValue(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return super.isAnchoringBlockAt(pos.relative(facing));
	}

	@Override
	protected ContraptionType getType() {
		return ContraptionType.GANTRY;
	}

	public Direction getFacing() {
		return facing;
	}

	@Override
	protected boolean shouldUpdateAfterMovement(BlockInfo info) {
		return super.shouldUpdateAfterMovement(info) && !AllBlocks.GANTRY_CARRIAGE.has(info.state);
	}

	@Override
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}
}
