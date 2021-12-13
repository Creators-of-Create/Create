package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A server to client version of {@link TileEntityConfigurationPacket}
 *
 * @param <TE>
 */
public abstract class TileEntityDataPacket<TE extends SyncedTileEntity> extends SimplePacketBase {

	protected BlockPos tilePos;

	public TileEntityDataPacket(FriendlyByteBuf buffer) {
		tilePos = buffer.readBlockPos();
	}

	public TileEntityDataPacket(BlockPos pos) {
		this.tilePos = pos;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(tilePos);
		writeData(buffer);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ClientLevel world = Minecraft.getInstance().level;

			if (world == null)
				return;

			BlockEntity tile = world.getBlockEntity(tilePos);

			if (tile instanceof SyncedTileEntity) {
				handlePacket((TE) tile);
			}
		});
		ctx.setPacketHandled(true);
	}

	protected abstract void writeData(FriendlyByteBuf buffer);

	protected abstract void handlePacket(TE tile);
}
