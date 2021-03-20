package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.foundation.config.AllConfigs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

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
	public boolean assemble(World world, BlockPos pos) throws AssemblyException {
		BlockPos offset = pos.offset(facing);
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
		return pos.equals(anchor.offset(facing.getOpposite()));
	}

	@Override
	public void addBlock(BlockPos pos, Pair<BlockInfo, TileEntity> capture) {
		BlockPos localPos = pos.subtract(anchor);
		if (!getBlocks().containsKey(localPos) && AllBlockTags.WINDMILL_SAILS.matches(capture.getKey().state))
			sailBlocks++;
		super.addBlock(pos, capture);
	}

	@Override
	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT tag = super.writeNBT(spawnPacket);
		tag.putInt("Sails", sailBlocks);
		tag.putInt("Facing", facing.getIndex());
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT tag, boolean spawnData) {
		sailBlocks = tag.getInt("Sails");
		facing = Direction.byIndex(tag.getInt("Facing"));
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new AnchoredLighter(this);
	}
}
