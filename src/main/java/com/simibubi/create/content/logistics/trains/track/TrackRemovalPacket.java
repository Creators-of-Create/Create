package com.simibubi.create.content.logistics.trains.track;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrackRemovalPacket extends SimplePacketBase {

	private Set<BlockPos> tracks;

	public TrackRemovalPacket(Set<BlockPos> tracks) {
		this.tracks = tracks;
	}

	public TrackRemovalPacket(FriendlyByteBuf buffer) {
		tracks = new HashSet<>();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			tracks.add(buffer.readBlockPos());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(tracks.size());
		tracks.forEach(buffer::writeBlockPos);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.getSender();
			Level level = sender.level;
			if (!AllItems.WRENCH.isIn(sender.getMainHandItem()))
				return;

			for (BlockPos blockPos : tracks) {
				BlockState blockState = level.getBlockState(blockPos);
				if (!blockPos.closerThan(sender.blockPosition(), 48))
					continue;
				if (!(blockState.getBlock()instanceof ITrackBlock track))
					continue;
				if (!sender.mayInteract(level, blockPos))
					continue;

				level.destroyBlock(blockPos, !sender.isCreative());
			}

			sender.displayClientMessage(new TextComponent("Tracks removed successfully"), true);
		});
		ctx.setPacketHandled(true);
	}

}
