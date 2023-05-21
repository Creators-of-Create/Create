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
	public static final int DEFAULT_MAX = 2_000_000;
	/**
	 * XL Packets expands the NBT packet limit to 2 GB.
	 */
	public static final int EXPANDED_MAX = 2_000_000_000;
	/**
	 * Minecart item sizes are limited by the vanilla slot change packet ({@link ClientboundContainerSetSlotPacket}).
	 * {@link ContraptionData#DEFAULT_MAX} is used as the default.
	 * XL Packets expands the size limit to ~2 GB. If the mod is loaded, we take advantage of it and use the higher limit.
	 */
	public static final int PICKUP_MAX = Mods.XLPACKETS.isLoaded() ? EXPANDED_MAX : DEFAULT_MAX;

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
