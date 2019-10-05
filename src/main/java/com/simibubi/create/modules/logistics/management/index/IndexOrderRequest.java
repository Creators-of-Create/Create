package com.simibubi.create.modules.logistics.management.index;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.packet.SimplePacketBase;
import com.simibubi.create.foundation.type.CountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.SupplyTask;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class IndexOrderRequest extends SimplePacketBase {

	String targetAddress;
	UUID networkID;
	CountedItemsList items;
	BlockPos indexPos;

	public IndexOrderRequest(BlockPos indexPos, String targetAddress, CountedItemsList list, UUID networkID) {
		this.targetAddress = targetAddress;
		items = list;
		this.networkID = networkID;
		this.indexPos = indexPos;
	}

	public IndexOrderRequest(PacketBuffer buffer) {
		indexPos = buffer.readBlockPos();
		networkID = new UUID(buffer.readLong(), buffer.readLong());
		targetAddress = buffer.readString(4096);
		items = new CountedItemsList();
		int numEntries = buffer.readInt();
		for (int j = 0; j < numEntries; j++)
			items.add(buffer.readItemStack(), buffer.readInt());
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(indexPos);
		buffer.writeLong(networkID.getMostSignificantBits());
		buffer.writeLong(networkID.getLeastSignificantBits());
		buffer.writeString(targetAddress, 4096);
		Collection<ItemStackEntry> entries = items.getFlattenedList();
		buffer.writeInt(entries.size());
		for (ItemStackEntry entry : entries) {
			buffer.writeItemStack(entry.stack);
			buffer.writeInt(entry.amount);
		}
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			TileEntity tileEntity = player.getEntityWorld().getTileEntity(indexPos);
			if (tileEntity != null && tileEntity instanceof LogisticalIndexTileEntity)
				((LogisticalIndexTileEntity) tileEntity).lastOrderAddress = targetAddress;
			LogisticalNetwork networkByID = Create.logisticalNetworkHandler.getNetworkByID(player.getEntityWorld(),
					networkID);
			items.getFlattenedList().forEach(entry -> networkByID.enqueueTask(new SupplyTask(entry, targetAddress)));
		});
		context.get().setPacketHandled(true);
	}

}
