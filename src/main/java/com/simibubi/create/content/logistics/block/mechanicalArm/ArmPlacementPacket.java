package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.Collection;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class ArmPlacementPacket extends SimplePacketBase {

	private Collection<ArmInteractionPoint> points;
	private ListTag receivedTag;
	private BlockPos pos;

	public ArmPlacementPacket(Collection<ArmInteractionPoint> points, BlockPos pos) {
		this.points = points;
		this.pos = pos;
	}

	public ArmPlacementPacket(FriendlyByteBuf buffer) {
		CompoundTag nbt = buffer.readNbt();
		receivedTag = nbt.getList("Points", Tag.TAG_COMPOUND);
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		CompoundTag nbt = new CompoundTag();
		ListTag pointsNBT = new ListTag();
		points.stream()
			.map(aip -> aip.serialize(pos))
			.forEach(pointsNBT::add);
		nbt.put("Points", pointsNBT);
		buffer.writeNbt(nbt);
		buffer.writeBlockPos(pos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			Level world = player.level;
			if (world == null || !world.isLoaded(pos))
				return;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof ArmBlockEntity))
				return;

			ArmBlockEntity arm = (ArmBlockEntity) blockEntity;
			arm.interactionPointTag = receivedTag;
		});
		return true;
	}

	public static class ClientBoundRequest extends SimplePacketBase {

		BlockPos pos;

		public ClientBoundRequest(BlockPos pos) {
			this.pos = pos;
		}

		public ClientBoundRequest(FriendlyByteBuf buffer) {
			this.pos = buffer.readBlockPos();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(pos);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> ArmInteractionPointHandler.flushSettings(pos)));
			return true;
		}

	}

}
