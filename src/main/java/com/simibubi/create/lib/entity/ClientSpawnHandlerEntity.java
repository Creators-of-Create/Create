package com.simibubi.create.lib.entity;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;

public interface ClientSpawnHandlerEntity {
	void onClientSpawn(ClientboundAddEntityPacket packet);
}
