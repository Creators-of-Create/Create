package com.simibubi.create.content.contraptions.bearing;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		if (isWindmill && sailBlocks < AllConfigs.server().kinetics.minimumWindmillSails.get())
			throw AssemblyException.notEnoughSails(sailBlocks);
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	public ContraptionType getType() {
		return ContraptionType.BEARING;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.relative(facing.getOpposite()));
	}

	@Override
	public void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
		BlockPos localPos = pos.subtract(anchor);
		if (!getBlocks().containsKey(localPos) && AllBlockTags.WINDMILL_SAILS.matches(getSailBlock(capture)))
			sailBlocks++;
		super.addBlock(pos, capture);
	}

	private BlockState getSailBlock(Pair<StructureBlockInfo, BlockEntity> capture) {
		BlockState state = capture.getKey().state;
		if (AllBlocks.COPYCAT_PANEL.has(state) && capture.getRight()instanceof CopycatBlockEntity cte)
			return cte.getMaterial();
		return state;
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new AnchoredLighter(this);
	}
}
