package com.simibubi.create.modules.contraptions.components.contraptions.pulley;

import com.simibubi.create.modules.contraptions.components.contraptions.AllContraptionTypes;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PulleyContraption extends Contraption {

	int initialOffset;

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.PULLEY;
	}

	public static PulleyContraption assemblePulleyAt(World world, BlockPos pos, int initialOffset) {
		if (isFrozen())
			return null;
		PulleyContraption construct = new PulleyContraption();
		construct.initialOffset = initialOffset;
		if (!construct.searchMovedStructure(world, pos, null))
			return null;
		construct.initActors(world);
		return construct;
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
	public CompoundNBT writeNBT() {
		CompoundNBT writeNBT = super.writeNBT();
		writeNBT.putInt("InitialOffset", initialOffset);
		return writeNBT;
	}

	@Override
	public void readNBT(World world, CompoundNBT nbt) {
		initialOffset = nbt.getInt("InitialOffset");
		super.readNBT(world, nbt);
	}

}
