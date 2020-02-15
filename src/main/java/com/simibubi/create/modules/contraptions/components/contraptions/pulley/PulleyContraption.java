package com.simibubi.create.modules.contraptions.components.contraptions.pulley;

import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PulleyContraption extends Contraption {

	int initialOffset;

	private static String type = "Pulley";

	static {
		register(type, PulleyContraption::new);
	}

	@Override
	protected String getType() {
		return type;
	}

	public static PulleyContraption assemblePulleyAt(World world, BlockPos pos, int initialOffset) {
		if (isFrozen())
			return null;
		PulleyContraption construct = new PulleyContraption();
		construct.initialOffset = initialOffset;
		if (!construct.searchMovedStructure(world, pos, Direction.DOWN))
			return null;
		construct.initActors(world);
		return construct;
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
