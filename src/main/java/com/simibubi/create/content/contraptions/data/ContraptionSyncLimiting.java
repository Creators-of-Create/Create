package com.simibubi.create.content.contraptions.data;

import com.simibubi.create.compat.Mods;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class ContraptionSyncLimiting {
	/**
	 * Contraption entity sync is limited by the clientbound custom payload limit, since that's what Forge's
	 * extended spawn packet uses. The NBT limit is irrelevant since it's bypassed on deserialization.
	 */
	public static final int SIZE_LIMIT = 1_048_576;

	// increased packet limits provided by other mods.
	public static final int PACKET_FIXER_LIMIT = SIZE_LIMIT * 100;
	public static final int XL_PACKETS_LIMIT = Integer.MAX_VALUE;

	// leave some room for the rest of the packet.
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
		return SIZE_LIMIT;
	}) - BUFFER;

	public static void writeSafe(CompoundTag compound, FriendlyByteBuf dst) {
		// Write the NBT, but take note of where we were before in case we need to roll back.
		int writerIndexBefore = dst.writerIndex();
		dst.writeNbt(compound);

		if (dst.writerIndex() > ContraptionSyncLimiting.LIMIT) {
			// Too large to fit in a packet, so roll back to where
			// we were before and write null so the client can detect it.
			dst.writerIndex(writerIndexBefore);
			dst.writeNbt(null);
		}
	}
}
