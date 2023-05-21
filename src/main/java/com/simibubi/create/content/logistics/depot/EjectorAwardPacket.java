package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class EjectorAwardPacket extends BlockEntityConfigurationPacket<EjectorBlockEntity> {

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
	protected void applySettings(ServerPlayer player, EjectorBlockEntity be) {
		AllAdvancements.EJECTOR_MAXED.awardTo(player);
	}

	@Override
	protected void applySettings(EjectorBlockEntity be) {}

}
