package com.simibubi.create.lib.block;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public interface CustomDataPacketHandlingBlockEntity {
	void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt);
}
