package com.simibubi.create.content.logistics.item;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.linked.LinkBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class LinkedControllerBindPacket extends LinkedControllerPacketBase {

	private int button;
	private BlockPos linkLocation;

	public LinkedControllerBindPacket(int button, BlockPos linkLocation) {
		super((BlockPos) null);
		this.button = button;
		this.linkLocation = linkLocation;
	}

	public LinkedControllerBindPacket(FriendlyByteBuf buffer) {
		super(buffer);
		this.button = buffer.readVarInt();
		this.linkLocation = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		super.write(buffer);
		buffer.writeVarInt(button);
		buffer.writeBlockPos(linkLocation);
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) {
		if (player.isSpectator())
			return;

		ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(heldItem);
		LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(player.level, linkLocation, LinkBehaviour.TYPE);
		if (linkBehaviour == null)
			return;

		linkBehaviour.getNetworkKey()
			.forEachWithContext((f, first) -> frequencyItems.setStackInSlot(button * 2 + (first ? 0 : 1), f.getStack()
				.copy()));

		heldItem.getTag()
			.put("Items", frequencyItems.serializeNBT());
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {}

}
