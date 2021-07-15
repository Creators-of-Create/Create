package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * A server to client version of {@link TileEntityConfigurationPacket}
 * 
 * @param <TE>
 */
public abstract class TileEntityDataPacket<TE extends SyncedTileEntity> extends SimplePacketBase {

	protected BlockPos tilePos;

	public TileEntityDataPacket(PacketBuffer buffer) {
		tilePos = buffer.readBlockPos();
	}

	public TileEntityDataPacket(BlockPos pos) {
		this.tilePos = pos;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(tilePos);
		writeData(buffer);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ClientWorld world = Minecraft.getInstance().level;

			if (world == null)
				return;

			TileEntity tile = world.getBlockEntity(tilePos);

			if (tile instanceof SyncedTileEntity) {
				handlePacket((TE) tile);
			}
		});
		ctx.setPacketHandled(true);
	}

	protected abstract void writeData(PacketBuffer buffer);

	protected abstract void handlePacket(TE tile);
}
