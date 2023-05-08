package com.simibubi.create.content.logistics.trains.entity;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRelocationPacket;
import com.simibubi.create.content.logistics.trains.track.BezierTrackPointLocation;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
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
import net.minecraftforge.network.PacketDistributor;

public class TrainRelocationPacket extends SimplePacketBase {

	UUID trainId;
	BlockPos pos;
	Vec3 lookAngle;
	int entityId;
	private boolean direction;
	private BezierTrackPointLocation hoveredBezier;

	public TrainRelocationPacket(FriendlyByteBuf buffer) {
		trainId = buffer.readUUID();
		pos = buffer.readBlockPos();
		lookAngle = VecHelper.read(buffer);
		entityId = buffer.readInt();
		direction = buffer.readBoolean();
		if (buffer.readBoolean())
			hoveredBezier = new BezierTrackPointLocation(buffer.readBlockPos(), buffer.readInt());
	}

	public TrainRelocationPacket(UUID trainId, BlockPos pos, BezierTrackPointLocation hoveredBezier, boolean direction,
		Vec3 lookAngle, int entityId) {
		this.trainId = trainId;
		this.pos = pos;
		this.hoveredBezier = hoveredBezier;
		this.direction = direction;
		this.lookAngle = lookAngle;
		this.entityId = entityId;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(trainId);
		buffer.writeBlockPos(pos);
		VecHelper.write(lookAngle, buffer);
		buffer.writeInt(entityId);
		buffer.writeBoolean(direction);
		buffer.writeBoolean(hoveredBezier != null);
		if (hoveredBezier != null) {
			buffer.writeBlockPos(hoveredBezier.curveTarget());
			buffer.writeInt(hoveredBezier.segment());
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
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

			int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 2;
			if (!sender.position()
				.closerThan(Vec3.atCenterOf(pos), verifyDistance)) {
				Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from clicked pos");
				return;
			}
			if (!sender.position()
				.closerThan(cce.position(), verifyDistance + cce.getBoundingBox()
					.getXsize() / 2)) {
				Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from carriage entity");
				return;
			}

			if (TrainRelocator.relocate(train, sender.level, pos, hoveredBezier, direction, lookAngle, false)) {
				sender.displayClientMessage(Lang.translateDirect("train.relocate.success")
					.withStyle(ChatFormatting.GREEN), true);
				train.carriages.forEach(c -> c.forEachPresentEntity(e -> {
					e.nonDamageTicks = 10;
					AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> e),
						new ContraptionRelocationPacket(e.getId()));
				}));
				return;
			}

			Create.LOGGER.warn(messagePrefix + train.name.getString() + ": relocation failed server-side");
		});
		return true;
	}

}
