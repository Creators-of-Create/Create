package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.TranslatingContraption;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class PulleyContraption extends TranslatingContraption {

	int initialOffset;

	@Override
	protected ContraptionType getType() {
		return ContraptionType.PULLEY;
	}

	public PulleyContraption() {}

	public PulleyContraption(int initialOffset) {
		this.initialOffset = initialOffset;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		startMoving(world);
		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		if (pos.getX() != anchor.getX() || pos.getZ() != anchor.getZ())
			return false;
		int y = pos.getY();
		if (y <= anchor.getY() || y > anchor.getY() + initialOffset + 1)
			return false;
		return true;
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialOffset", initialOffset);
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		initialOffset = nbt.getInt("InitialOffset");
		super.readNBT(world, nbt, spawnData);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new PulleyLighter(this);
	}
}
