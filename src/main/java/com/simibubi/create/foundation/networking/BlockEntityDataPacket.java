package com.simibubi.create.foundation.networking;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * A server to client version of {@link BlockEntityConfigurationPacket}
 * 
 * @param <BE>
 */
public abstract class BlockEntityDataPacket<BE extends SyncedBlockEntity> extends SimplePacketBase {

	protected BlockPos pos;

	public BlockEntityDataPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
	}

	public BlockEntityDataPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		writeData(buffer);
	}

	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			ClientLevel world = Minecraft.getInstance().level;

			if (world == null)
				return;

			BlockEntity blockEntity = world.getBlockEntity(pos);

			if (blockEntity instanceof SyncedBlockEntity) {
				handlePacket((BE) blockEntity);
			}
		});
		return true;
	}

	protected abstract void writeData(FriendlyByteBuf buffer);

	protected abstract void handlePacket(BE blockEntity);
}
