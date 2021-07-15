package com.simibubi.create.content.logistics.item.filter;

import java.util.function.Supplier;

import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class FilterScreenPacket extends SimplePacketBase {

	public enum Option {
		WHITELIST, WHITELIST2, BLACKLIST, RESPECT_DATA, IGNORE_DATA, UPDATE_FILTER_ITEM, ADD_TAG, ADD_INVERTED_TAG;
	}

	private final Option option;
	private final CompoundNBT data;

	public FilterScreenPacket(Option option) {
		this(option, new CompoundNBT());
	}

	public FilterScreenPacket(Option option, CompoundNBT data) {
		this.option = option;
		this.data = data;
	}

	public FilterScreenPacket(PacketBuffer buffer) {
		option = Option.values()[buffer.readInt()];
		data = buffer.readNbt();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(option.ordinal());
		buffer.writeNbt(data);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;

			if (player.containerMenu instanceof FilterContainer) {
				FilterContainer c = (FilterContainer) player.containerMenu;
				if (option == Option.WHITELIST)
					c.blacklist = false;
				if (option == Option.BLACKLIST)
					c.blacklist = true;
				if (option == Option.RESPECT_DATA)
					c.respectNBT = true;
				if (option == Option.IGNORE_DATA)
					c.respectNBT = false;
				if (option == Option.UPDATE_FILTER_ITEM)
					c.ghostInventory.setStackInSlot(
							data.getInt("Slot"),
							net.minecraft.item.ItemStack.of(data.getCompound("Item")));
			}

			if (player.containerMenu instanceof AttributeFilterContainer) {
				AttributeFilterContainer c = (AttributeFilterContainer) player.containerMenu;
				if (option == Option.WHITELIST)
					c.whitelistMode = WhitelistMode.WHITELIST_DISJ;
				if (option == Option.WHITELIST2)
					c.whitelistMode = WhitelistMode.WHITELIST_CONJ;
				if (option == Option.BLACKLIST)
					c.whitelistMode = WhitelistMode.BLACKLIST;
				if (option == Option.ADD_TAG)
					c.appendSelectedAttribute(ItemAttribute.fromNBT(data), false);
				if (option == Option.ADD_INVERTED_TAG)
					c.appendSelectedAttribute(ItemAttribute.fromNBT(data), true);
			}

		});
		context.get().setPacketHandled(true);
	}

}
