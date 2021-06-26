package com.simibubi.create.foundation.utility;

import net.minecraft.nbt.CompoundNBT;

public interface IPartialSafeNBT {
	public void writeSafe(CompoundNBT compound, boolean clientPacket);
}
