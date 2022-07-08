package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class EjectorAwardPacket extends TileEntityConfigurationPacket<EjectorTileEntity> {

	public EjectorAwardPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	public EjectorAwardPacket(BlockPos pos) {
		super(pos);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(ServerPlayer player, EjectorTileEntity te) {
		AllAdvancements.EJECTOR_MAXED.awardTo(player);
	}

	@Override
	protected void applySettings(EjectorTileEntity te) {}

}
