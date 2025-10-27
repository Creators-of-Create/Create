package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllPackets;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public record PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) implements ServerboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, PackagePortPlacementPacket> STREAM_CODEC = StreamCodec.composite(
	    PackagePortTarget.STREAM_CODEC, PackagePortPlacementPacket::target,
	    BlockPos.STREAM_CODEC, PackagePortPlacementPacket::pos,
	    PackagePortPlacementPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PLACE_PACKAGE_PORT;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (player == null)
			return;
		Level world = player.level();
		if (world == null || !world.isLoaded(pos))
			return;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof PackagePortBlockEntity ppbe))
			return;
		if (!target.canSupport(ppbe))
			return;

		Vec3 targetLocation = target.getExactTargetLocation(ppbe, world, pos);
		if (targetLocation == Vec3.ZERO || !targetLocation.closerThan(Vec3.atBottomCenterOf(pos),
			AllConfigs.server().logistics.packagePortRange.get() + 2))
			return;

		target.setup(ppbe, world, pos);
		ppbe.target = target;
		ppbe.notifyUpdate();
		ppbe.use(player);
	}

	public record ClientBoundRequest(BlockPos pos) implements ClientboundPacketPayload {
		public static final StreamCodec<ByteBuf, ClientBoundRequest> STREAM_CODEC = BlockPos.STREAM_CODEC
			.map(ClientBoundRequest::new, ClientBoundRequest::pos);

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.S_PLACE_PACKAGE_PORT;
		}

		@Override
		public void handle(LocalPlayer player) {
			PackagePortTargetSelectionHandler.flushSettings(pos);
		}
	}
}
