package com.simibubi.create.modules.logistics.management.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.packet.SimplePacketBase;
import com.simibubi.create.foundation.type.CountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class IndexContainerUpdatePacket extends SimplePacketBase {

	public enum Type {
		INITIAL, UPDATE
	}

	Type type;
	List<Pair<String, CountedItemsList>> items;
	BlockPos pos;

	public IndexContainerUpdatePacket(Type type, String address, CountedItemsList items, BlockPos pos) {
		this(type, Arrays.asList(Pair.of(address, items)), pos);
	}
	
	public IndexContainerUpdatePacket(Type type, List<Pair<String, CountedItemsList>> items, BlockPos pos) {
		this.type = type;
		this.items = items;
		this.pos = pos;
	}

	public IndexContainerUpdatePacket(PacketBuffer buffer) {
		type = Type.values()[buffer.readInt()];
		int numControllers = buffer.readInt();
		items = new ArrayList<>(numControllers);
		for (int i = 0; i < numControllers; i++) {
			String address = buffer.readString(4096);
			CountedItemsList itemList = new CountedItemsList();
			int numEntries = buffer.readInt();
			for (int j = 0; j < numEntries; j++)
				itemList.add(buffer.readItemStack(), buffer.readInt());
			items.add(Pair.of(address, itemList));
		}
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(type.ordinal());
		buffer.writeInt(items.size());
		for (Pair<String, CountedItemsList> pair : items) {
			buffer.writeString(pair.getKey(), 4096);
			Collection<ItemStackEntry> entries = pair.getValue().getFlattenedList();
			buffer.writeInt(entries.size());
			for (ItemStackEntry entry : entries) {
				buffer.writeItemStack(entry.stack);
				buffer.writeInt(entry.amount);
			}
		}
		buffer.writeBlockPos(pos);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Container openContainer = mc.player.openContainer;
			if (openContainer == null)
				return;
			if (!(openContainer instanceof LogisticalIndexContainer))
				return;
			LogisticalIndexTileEntity te = (LogisticalIndexTileEntity) mc.world.getTileEntity(pos);
			if (te == null)
				return;

			if (type == Type.INITIAL) 
				te.index(items);
			if (type == Type.UPDATE) 
				te.update(items);
			
			((LogisticalIndexContainer) openContainer).refresh();
		});
		context.get().setPacketHandled(true);
	}

}
