package com.simibubi.create.content.contraptions.data;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.mixin.accessor.NbtAccounterAccessor;

import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class ContraptionPickupLimiting {
	/// The default NBT limit, defined by {@link FriendlyByteBuf#readNbt()}.
	public static final int NBT_LIMIT = 2_097_152;

	// increased nbt limits provided by other mods.
	public static final int PACKET_FIXER_LIMIT = NBT_LIMIT * 100;
	public static final int XL_PACKETS_LIMIT = Integer.MAX_VALUE;

	// leave some space for the rest of the packet.
	public static final int BUFFER = 20_000;

	// the actual limit to be used
	public static final int LIMIT = Util.make(() -> {
		// the smallest limit needs to be used, as we can't guarantee that all mixins are applied if multiple are present.
		if (Mods.PACKETFIXER.isLoaded()) {
			return PACKET_FIXER_LIMIT;
		} else if (Mods.XLPACKETS.isLoaded()) {
			return XL_PACKETS_LIMIT;
		}

		// none are present, use vanilla default
		return NBT_LIMIT;
	}) - BUFFER;

	/**
	 * @return true if the given NBT is too large for a contraption to be picked up with a wrench.
	 */
	public static boolean isTooLargeForPickup(Tag data) {
		return nbtSize(data) > LIMIT;
	}

	/**
	 * @return the size of the given NBT when read by the client according to {@link NbtAccounter}
	 */
	private static long nbtSize(Tag data) {
		FriendlyByteBuf test = new FriendlyByteBuf(Unpooled.buffer());
		test.writeNbt(data);
		NbtAccounter sizeTracker = NbtAccounter.unlimitedHeap();
		test.readNbt(sizeTracker);
		long size = ((NbtAccounterAccessor) sizeTracker).create$getUsage();
		test.release();
		return size;
	}
}
