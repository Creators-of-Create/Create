package com.simibubi.create.foundation.utility;

import net.minecraft.nbt.CompoundTag;

public interface IPartialSafeNBT {
	public void writeSafe(CompoundTag compound, boolean clientPacket);
}
