package com.simibubi.create.foundation.networking;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import com.simibubi.create.foundation.utility.AdventureUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public abstract class BlockEntityConfigurationPacket<BE extends SyncedBlockEntity> implements ServerboundPacketPayload {
	protected final BlockPos pos;

	public BlockEntityConfigurationPacket(BlockPos pos) {
		this.pos = pos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(ServerPlayer player) {
		if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
			return;
		Level world = player.level();
		if (!world.isLoaded(this.pos))
			return;
		if (!this.pos.closerThan(player.blockPosition(), maxRange()))
			return;
		BlockEntity blockEntity = world.getBlockEntity(this.pos);
		if (blockEntity instanceof SyncedBlockEntity) {
			applySettings(player, (BE) blockEntity);
			if (!causeUpdate())
				return;
			((SyncedBlockEntity) blockEntity).sendData();
			blockEntity.setChanged();
		}
	}

	protected int maxRange() {
		return 20;
	}

	protected boolean causeUpdate() {
		return true;
	}

	protected abstract void applySettings(ServerPlayer player, BE be);
}
