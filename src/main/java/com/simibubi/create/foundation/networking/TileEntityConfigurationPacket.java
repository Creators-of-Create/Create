package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public void write(FriendlyByteBuf buffer) {
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
				if (!pos.closerThan(player.blockPosition(), maxRange()))
					return;
				BlockEntity tileEntity = world.getBlockEntity(pos);
				if (tileEntity instanceof SyncedTileEntity) {
					applySettings(player, (TE) tileEntity);
					if (!causeUpdate())
						return;
					((SyncedTileEntity) tileEntity).sendData();
					tileEntity.setChanged();
				}
			});
		context.get()
			.setPacketHandled(true);

	}

	protected int maxRange() {
		return 20;
	}

	protected abstract void writeSettings(FriendlyByteBuf buffer);

	protected abstract void readSettings(FriendlyByteBuf buffer);

	protected void applySettings(ServerPlayer player, TE te) {
		applySettings(te);
	}
	
	protected boolean causeUpdate() {
		return true;
	}

	protected abstract void applySettings(TE te);

}
