package com.simibubi.create.lib.block;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public interface CustomDataPacketHandlingTileEntity {
	void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt);
}
