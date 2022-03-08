package com.simibubi.create.content.logistics.trains.entity;

import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrainRelocationPacket extends SimplePacketBase {

	UUID trainId;
	BlockPos pos;
	Vec3 lookAngle;
	int entityId;

	public TrainRelocationPacket(FriendlyByteBuf buffer) {
		trainId = buffer.readUUID();
		pos = buffer.readBlockPos();
		lookAngle = VecHelper.read(buffer);
		entityId = buffer.readInt();
	}

	public TrainRelocationPacket(UUID trainId, BlockPos pos, Vec3 lookAngle, int entityId) {
		this.trainId = trainId;
		this.pos = pos;
		this.lookAngle = lookAngle;
		this.entityId = entityId;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(trainId);
		buffer.writeBlockPos(pos);
		VecHelper.write(lookAngle, buffer);
		buffer.writeInt(entityId);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.getSender();
			Train train = Create.RAILWAYS.trains.get(trainId);
			Entity entity = sender.level.getEntity(entityId);

			String messagePrefix = sender.getName()
				.getString() + " could not relocate Train ";

			if (train == null || !(entity instanceof CarriageContraptionEntity cce)) {
				Create.LOGGER.warn(messagePrefix + train.id.toString()
					.substring(0, 5) + ": not present on server");
				return;
			}

			if (!train.id.equals(cce.trainId))
				return;

			if (!sender.position()
				.closerThan(Vec3.atCenterOf(pos), 26)) {
				Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from clicked pos");
				return;
			}
			if (!sender.position()
				.closerThan(cce.position(), 26 + cce.getBoundingBox()
					.getXsize() / 2)) {
				Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from carriage entity");
				return;
			}

			if (TrainRelocator.relocate(train, sender.level, pos, lookAngle, false)) {
				sender.displayClientMessage(Lang.translate("train.relocate.success")
					.withStyle(ChatFormatting.GREEN), true);
				train.syncTrackGraphChanges();
				return;
			}

			Create.LOGGER.warn(messagePrefix + train.name.getString() + ": relocation failed server-side");

		});
		ctx.setPacketHandled(true);
	}

}
