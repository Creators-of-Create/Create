package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.TranslatingContraption;

import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PulleyContraption extends TranslatingContraption {

	int initialOffset;

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.PULLEY;
	}

	public PulleyContraption() {}

	public PulleyContraption(int initialOffset) {
		this.initialOffset = initialOffset;
	}

	@Override
	public boolean assemble(World world, BlockPos pos) {
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
	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialOffset", initialOffset);
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT nbt, boolean spawnData) {
		initialOffset = nbt.getInt("InitialOffset");
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	public ContraptionLighter<?> makeLighter() {
		return new PulleyLighter(this);
	}
}
