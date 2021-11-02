package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.Collection;
import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

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
		receivedTag = nbt.getList("Points", NBT.TAG_COMPOUND);
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
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer player = context.get()
					.getSender();
				if (player == null)
					return;
				Level world = player.level;
				if (world == null || !world.isLoaded(pos))
					return;
				BlockEntity tileEntity = world.getBlockEntity(pos);
				if (!(tileEntity instanceof ArmTileEntity))
					return;

				ArmTileEntity arm = (ArmTileEntity) tileEntity;
				arm.interactionPointTag = receivedTag;
			});
		context.get()
			.setPacketHandled(true);

	}

}
