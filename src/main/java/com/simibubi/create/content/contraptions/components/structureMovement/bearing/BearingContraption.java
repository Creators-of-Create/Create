package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BearingContraption extends Contraption {

	protected int sailBlocks;
	protected Direction facing;

	private boolean isWindmill;

	public BearingContraption() {}

	public BearingContraption(boolean isWindmill, Direction facing) {
		this.isWindmill = isWindmill;
		this.facing = facing;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		BlockPos offset = pos.relative(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		expandBoundsAroundAxis(facing.getAxis());
		if (isWindmill && sailBlocks < AllConfigs.SERVER.kinetics.minimumWindmillSails.get())
			throw AssemblyException.notEnoughSails(sailBlocks);
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	protected ContraptionType getType() {
		return ContraptionType.BEARING;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.relative(facing.getOpposite()));
	}

	@Override
	public void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
		BlockPos localPos = pos.subtract(anchor);
		if (!getBlocks().containsKey(localPos) && AllBlockTags.WINDMILL_SAILS.matches(capture.getKey().state))
			sailBlocks++;
		super.addBlock(pos, capture);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("Sails", sailBlocks);
		tag.putInt("Facing", facing.get3DDataValue());
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag tag, boolean spawnData) {
		sailBlocks = tag.getInt("Sails");
		facing = Direction.from3DDataValue(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}

	public int getSailBlocks() {
		return sailBlocks;
	}

	public Direction getFacing() {
		return facing;
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		if (facing.getOpposite() == this.facing && BlockPos.ZERO.equals(localPos))
			return false;
		return facing.getAxis() == this.facing.getAxis();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new AnchoredLighter(this);
	}
}
