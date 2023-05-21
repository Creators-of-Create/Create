package com.simibubi.create.content.contraptions.gantry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.TranslatingContraption;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;
import com.simibubi.create.content.contraptions.render.NonStationaryLighter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GantryContraption extends TranslatingContraption {

	protected Direction facing;

	public GantryContraption() {}

	public GantryContraption(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		startMoving(world);
		return true;
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("Facing", facing.get3DDataValue());
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag tag, boolean spawnData) {
		facing = Direction.from3DDataValue(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return super.isAnchoringBlockAt(pos.relative(facing));
	}

	@Override
	public ContraptionType getType() {
		return ContraptionType.GANTRY;
	}

	public Direction getFacing() {
		return facing;
	}

	@Override
	protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
		return super.shouldUpdateAfterMovement(info) && !AllBlocks.GANTRY_CARRIAGE.has(info.state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}
}
