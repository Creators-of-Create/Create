package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionInteractionPacket extends SimplePacketBase {

	private InteractionHand interactionHand;
	private int target;
	private BlockPos localPos;
	private Direction face;

	public ContraptionInteractionPacket(AbstractContraptionEntity target, InteractionHand hand, BlockPos localPos, Direction side) {
		this.interactionHand = hand;
		this.localPos = localPos;
		this.target = target.getId();
		this.face = side;
	}

	public ContraptionInteractionPacket(FriendlyByteBuf buffer) {
		target = buffer.readInt();
		int handId = buffer.readInt();
		interactionHand = handId == -1 ? null : InteractionHand.values()[handId];
		localPos = buffer.readBlockPos();
		face = Direction.from3DDataValue(buffer.readShort());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(target);
		buffer.writeInt(interactionHand == null ? -1 : interactionHand.ordinal());
		buffer.writeBlockPos(localPos);
		buffer.writeShort(face.get3DDataValue());
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer sender = context.get().getSender();
			if (sender == null)
				return;
			Entity entityByID = sender.getLevel().getEntity(target);
			if (!(entityByID instanceof AbstractContraptionEntity))
				return;
			AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
			double d = sender.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() + 10;
			if (!sender.canSee(entityByID))
				d -= 3;
			d *= d;
			if (sender.distanceToSqr(entityByID) > d) {
				// TODO log?
				return;
			}
			if (contraptionEntity.handlePlayerInteraction(sender, localPos, face, interactionHand))
				sender.swing(interactionHand, true);
		});
		context.get().setPacketHandled(true);
	}

}
