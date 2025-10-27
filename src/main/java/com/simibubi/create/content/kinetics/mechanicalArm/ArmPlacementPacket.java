package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.Collection;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ArmPlacementPacket(ListTag tag, BlockPos pos) implements ServerboundPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmPlacementPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecs.COMPOUND_LIST_TAG, ArmPlacementPacket::tag,
			BlockPos.STREAM_CODEC, ArmPlacementPacket::pos,
			ArmPlacementPacket::new
	);

	public ArmPlacementPacket(Collection<ArmInteractionPoint> points, BlockPos pos) {
		this(new ListTag(), pos);

		for (ArmInteractionPoint point : points) {
			this.tag.add(point.serialize(pos));
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PLACE_ARM;
	}

	@Override
	public void handle(ServerPlayer player) {
		Level world = player.level();
		if (!world.isLoaded(pos))
			return;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof ArmBlockEntity arm))
			return;

		arm.interactionPointTag = this.tag;
	}

	public record ClientBoundRequest(BlockPos pos) implements ClientboundPacketPayload {
		public static final StreamCodec<ByteBuf, ClientBoundRequest> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
				ClientBoundRequest::new, ClientBoundRequest::pos
		);

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.S_PLACE_ARM;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(LocalPlayer player) {
			ArmInteractionPointHandler.flushSettings(pos);
		}
	}

}
