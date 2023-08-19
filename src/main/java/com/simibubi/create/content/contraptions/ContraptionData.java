package com.simibubi.create.content.contraptions;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.mixin.accessor.NbtAccounterAccessor;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

public class ContraptionData {
	/**
	 * A sane, default maximum for contraption data size.
	 */
	public static final int DEFAULT_LIMIT = 2_000_000;
	/**
	 * Connectivity expands the NBT packet limit to 2 GB.
	 */
	public static final int CONNECTIVITY_LIMIT = Integer.MAX_VALUE;
	/**
	 * Packet Fixer expands the NBT packet limit to 200 MB.
	 */
	public static final int PACKET_FIXER_LIMIT = 209_715_200;
	/**
	 * XL Packets expands the NBT packet limit to 2 GB.
	 */
	public static final int XL_PACKETS_LIMIT = 2_000_000_000;
	/**
	 * Minecart item sizes are limited by the vanilla slot change packet ({@link ClientboundContainerSetSlotPacket}).
	 * {@link #DEFAULT_LIMIT} is used as the default.
	 * Connectivity, PacketFixer, and XL Packets expand the size limit.
	 * If one of these mods is loaded, we take advantage of it and use the higher limit.
	 */
	public static final int PICKUP_LIMIT;

	static {
		int limit = DEFAULT_LIMIT;

		// Check from largest to smallest to use the smallest limit if multiple mods are loaded.
		// It is necessary to use the smallest limit because even if multiple mods are loaded,
		// not all of their mixins may be applied. Therefore, it is safest to only assume that
		// the mod with the smallest limit is actually active.
		if (Mods.CONNECTIVITY.isLoaded()) {
			limit = CONNECTIVITY_LIMIT;
		}
		if (Mods.XLPACKETS.isLoaded()) {
			limit = XL_PACKETS_LIMIT;
		}
		if (Mods.PACKETFIXER.isLoaded()) {
			limit = PACKET_FIXER_LIMIT;
		}

		PICKUP_LIMIT = limit;
	}

	/**
	 * @return true if the given NBT is too large for a contraption to be synced to clients.
	 */
	public static boolean isTooLargeForSync(CompoundTag data) {
		int max = AllConfigs.server().kinetics.maxDataSize.get();
		return max != 0 && packetSize(data) > max;
	}

	/**
	 * @return true if the given NBT is too large for a contraption to be picked up with a wrench.
	 */
	public static boolean isTooLargeForPickup(CompoundTag data) {
		return packetSize(data) > PICKUP_LIMIT;
	}

	/**
	 * @return the size of the given NBT when put through a packet, in bytes.
	 */
	public static long packetSize(CompoundTag data) {
		FriendlyByteBuf test = new FriendlyByteBuf(Unpooled.buffer());
		test.writeNbt(data);
		NbtAccounter sizeTracker = new NbtAccounter(Long.MAX_VALUE);
		test.readNbt(sizeTracker);
		long size = ((NbtAccounterAccessor) sizeTracker).create$getUsage();
		test.release();
		return size;
	}
}
