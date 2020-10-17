package com.simibubi.create.content.logistics.item.filter;

import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class FilterScreenPacket extends SimplePacketBase {

	public enum Option {
		CLEAR, WHITELIST, WHITELIST2, BLACKLIST, RESPECT_DATA, IGNORE_DATA, UPDATE_FILTER_ITEM, ADD_TAG, ADD_INVERTED_TAG;
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
		data = buffer.readCompoundTag();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(option.ordinal());
		buffer.writeCompoundTag(data);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;

			if (player.openContainer instanceof AbstractFilterContainer) {
				AbstractFilterContainer c = (AbstractFilterContainer) player.openContainer;
				if (option == Option.CLEAR) {
					c.clearContents();
					return;
				}
			}

			if (player.openContainer instanceof FilterContainer) {
				FilterContainer c = (FilterContainer) player.openContainer;
				if (option == Option.WHITELIST)
					c.blacklist = false;
				if (option == Option.BLACKLIST)
					c.blacklist = true;
				if (option == Option.RESPECT_DATA)
					c.respectNBT = true;
				if (option == Option.IGNORE_DATA)
					c.respectNBT = false;
				if (option == Option.UPDATE_FILTER_ITEM)
					c.filterInventory.setStackInSlot(
							data.getInt("Slot"),
							net.minecraft.item.ItemStack.read(data.getCompound("Item")));
			}

			if (player.openContainer instanceof AttributeFilterContainer) {
				AttributeFilterContainer c = (AttributeFilterContainer) player.openContainer;
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
