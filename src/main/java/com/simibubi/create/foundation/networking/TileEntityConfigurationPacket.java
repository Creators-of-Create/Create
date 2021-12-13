package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class TileEntityConfigurationPacket<TE extends SyncedTileEntity> extends SimplePacketBase {

	protected BlockPos pos;

	public TileEntityConfigurationPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		readSettings(buffer);
	}

	public TileEntityConfigurationPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		writeSettings(buffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer player = context.get()
					.getSender();
				if (player == null)
					return;
				Level world = player.level;

				if (world == null || !world.isLoaded(pos))
					return;
				BlockEntity tileEntity = world.getBlockEntity(pos);
				if (tileEntity instanceof SyncedTileEntity) {
					applySettings((TE) tileEntity);
					((SyncedTileEntity) tileEntity).sendData();
					tileEntity.setChanged();
				}
			});
		context.get()
			.setPacketHandled(true);

	}

	protected abstract void writeSettings(FriendlyByteBuf buffer);

	protected abstract void readSettings(FriendlyByteBuf buffer);

	protected abstract void applySettings(TE te);

}
