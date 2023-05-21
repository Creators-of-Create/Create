package com.simibubi.create.content.contraptions.bearing;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;
import com.simibubi.create.content.contraptions.render.NonStationaryLighter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StabilizedContraption extends Contraption {

	private Direction facing;

	public StabilizedContraption() {}

	public StabilizedContraption(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		BlockPos offset = pos.relative(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	public ContraptionType getType() {
		return ContraptionType.STABILIZED;
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
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}

	public Direction getFacing() {
		return facing;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}
}
