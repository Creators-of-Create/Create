package com.simibubi.create.content.logistics.filter;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record FilterScreenPacket(Option option, @Nullable CompoundTag data) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, FilterScreenPacket> STREAM_CODEC = StreamCodec.composite(
			Option.STREAM_CODEC, FilterScreenPacket::option,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.COMPOUND_TAG), FilterScreenPacket::data,
			FilterScreenPacket::new
	);

	public FilterScreenPacket(Option option) {
		this(option, null);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_FILTER;
	}

	@Override
	public void handle(ServerPlayer player) {
		CompoundTag tag = this.data == null ? new CompoundTag() : this.data;

		if (player.containerMenu instanceof FilterMenu c) {
			if (this.option == Option.WHITELIST)
				c.blacklist = false;
			if (this.option == Option.BLACKLIST)
				c.blacklist = true;
			if (this.option == Option.RESPECT_DATA)
				c.respectNBT = true;
			if (this.option == Option.IGNORE_DATA)
				c.respectNBT = false;
			if (this.option == Option.UPDATE_FILTER_ITEM)
				c.ghostInventory.setStackInSlot(
					tag.getInt("Slot"),
					ItemStack.parseOptional(player.registryAccess(), tag.getCompound("Item"))
				);
		}

		if (player.containerMenu instanceof AttributeFilterMenu c) {
			if (option == Option.WHITELIST)
				c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
			if (option == Option.WHITELIST2)
				c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
			if (option == Option.BLACKLIST)
				c.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
			if (option == Option.ADD_TAG)
				c.appendSelectedAttribute(ItemAttribute.loadStatic(data, player.registryAccess()), false);
			if (option == Option.ADD_INVERTED_TAG)
				c.appendSelectedAttribute(ItemAttribute.loadStatic(data, player.registryAccess()), true);
		}

		if (player.containerMenu instanceof PackageFilterMenu c) {
			if (option == Option.UPDATE_ADDRESS)
				c.address = tag.getString("Address");
		}
	}

	public enum Option {
		WHITELIST, WHITELIST2, BLACKLIST, RESPECT_DATA, IGNORE_DATA, UPDATE_FILTER_ITEM, ADD_TAG, ADD_INVERTED_TAG, UPDATE_ADDRESS;

		public static final StreamCodec<ByteBuf, Option> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
	}
}
