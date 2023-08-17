package com.simibubi.create.content.contraptions;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.mixin.accessor.NbtAccounterAccessor;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ContraptionData {
	/**
	 * A sane, default maximum for contraption data size.
	 */
	public static final int DEFAULT_MAX = 2_000_000;
	/**
	 * Connectivity expands the NBT packet limit to 2 GB.
	 */
	public static final int EXPANDED_MAX_CONNECTIVITY = Integer.MAX_VALUE;
	/**
	 * XL Packets expands the NBT packet limit to 2 GB.
	 */
	public static final int EXPANDED_MAX_XL_PACKETS = 2_000_000_000;
	/**
	 * Packet Fixer expands the NBT packet limit to 200 MB.
	 */
	public static final int EXPANDED_MAX_PACKET_FIXER = 209_715_200;
	/**
	 * Minecart item sizes are limited by the vanilla slot change packet ({@link ClientboundContainerSetSlotPacket}).
	 * {@link ContraptionData#DEFAULT_MAX} is used as the default.
	 * Some network optimisation mods expand the size limit:
	 * Connectivity to Integer.MAX_VALUE = 2_147_483_647 ~= 2GB.
	 * XL Packets to 2_000_000_000 bytes ~= 2GB.
	 * Packet Fixer to 209_715_200 bytes ~= 200MB.
	 * If some of these mods are loaded, we take advantage of it and use the higher limit.
	 */
	public static final int PICKUP_MAX = getMaxPickUpSize();


	/**
	 * @return max contraption data size depending on packet size optimisation mods loaded.
	 */
	public static int getMaxPickUpSize() {
		Set<Integer> values = new HashSet<>();

		if (Mods.CONNECTIVITY.isLoaded()) {
			values.add(EXPANDED_MAX_CONNECTIVITY);
		}
		if (Mods.XLPACKETS.isLoaded()) {
			values.add(EXPANDED_MAX_XL_PACKETS);
		}
		if (Mods.PACKETFIXER.isLoaded()) {
			values.add(EXPANDED_MAX_PACKET_FIXER);
		}

		if (values.isEmpty()) {
			values.add(DEFAULT_MAX);
		}
		return Collections.min(values);
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
		return packetSize(data) > PICKUP_MAX;
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
