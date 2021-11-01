package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class EjectorTriggerPacket extends TileEntityConfigurationPacket<EjectorTileEntity> {

	public EjectorTriggerPacket(BlockPos pos) {
		super(pos);
	}
	
	public EjectorTriggerPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(EjectorTileEntity te) {
		te.activate();
	}

}
