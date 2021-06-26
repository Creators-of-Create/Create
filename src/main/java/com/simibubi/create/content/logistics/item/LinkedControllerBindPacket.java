package com.simibubi.create.content.logistics.item;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;

public class LinkedControllerBindPacket extends LinkedControllerPacketBase {

	private int button;
	private BlockPos linkLocation;

	public LinkedControllerBindPacket(int button, BlockPos linkLocation) {
		this.button = button;
		this.linkLocation = linkLocation;
	}

	public LinkedControllerBindPacket(PacketBuffer buffer) {
		this.button = buffer.readVarInt();
		this.linkLocation = buffer.readBlockPos();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeVarInt(button);
		buffer.writeBlockPos(linkLocation);
	}

	@Override
	protected void handle(ServerPlayerEntity player, ItemStack heldItem) {
		if (player.isSpectator())
			return;

		ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(heldItem);
		LinkBehaviour linkBehaviour = TileEntityBehaviour.get(player.world, linkLocation, LinkBehaviour.TYPE);
		if (linkBehaviour == null)
			return;
		
		Pair<Frequency, Frequency> pair = linkBehaviour.getNetworkKey();
		frequencyItems.setStackInSlot(button * 2, pair.getKey()
			.getStack()
			.copy());
		frequencyItems.setStackInSlot(button * 2 + 1, pair.getValue()
			.getStack()
			.copy());

		heldItem.getTag()
			.put("Items", frequencyItems.serializeNBT());
	}

}
