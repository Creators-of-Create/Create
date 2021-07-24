package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class TileEntityConfigurationPacket<TE extends SyncedTileEntity> extends SimplePacketBase {

	protected BlockPos pos;

	public TileEntityConfigurationPacket(PacketBuffer buffer) {
		pos = buffer.readBlockPos();
		readSettings(buffer);
	}

	public TileEntityConfigurationPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		writeSettings(buffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;
				World world = player.level;

				if (world == null || !world.isLoaded(pos))
					return;
				TileEntity tileEntity = world.getBlockEntity(pos);
				if (tileEntity instanceof SyncedTileEntity) {
					applySettings((TE) tileEntity);
					((SyncedTileEntity) tileEntity).sendData();
					tileEntity.setChanged();
				}
			});
		context.get()
			.setPacketHandled(true);

	}

	protected abstract void writeSettings(PacketBuffer buffer);

	protected abstract void readSettings(PacketBuffer buffer);

	protected abstract void applySettings(TE te);

}
