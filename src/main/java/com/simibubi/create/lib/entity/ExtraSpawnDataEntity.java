package com.simibubi.create.lib.entity;

import net.minecraft.network.FriendlyByteBuf;

public interface ExtraSpawnDataEntity {
	void readSpawnData(FriendlyByteBuf buf);

	void writeSpawnData(FriendlyByteBuf buf);
}
