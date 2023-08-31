package com.simibubi.create.content.equipment.extendoGrip;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent.Context;

public class ExtendoGripInteractionPacket extends SimplePacketBase {

	private InteractionHand interactionHand;
	private int target;
	private Vec3 specificPoint;

	public ExtendoGripInteractionPacket(Entity target) {
		this(target, null);
	}

	public ExtendoGripInteractionPacket(Entity target, InteractionHand hand) {
		this(target, hand, null);
	}

	public ExtendoGripInteractionPacket(Entity target, InteractionHand hand, Vec3 specificPoint) {
		interactionHand = hand;
		this.specificPoint = specificPoint;
		this.target = target.getId();
	}

	public ExtendoGripInteractionPacket(FriendlyByteBuf buffer) {
		target = buffer.readInt();
		int handId = buffer.readInt();
		interactionHand = handId == -1 ? null : InteractionHand.values()[handId];
		if (buffer.readBoolean())
			specificPoint = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(target);
		buffer.writeInt(interactionHand == null ? -1 : interactionHand.ordinal());
		buffer.writeBoolean(specificPoint != null);
		if (specificPoint != null) {
			buffer.writeDouble(specificPoint.x);
			buffer.writeDouble(specificPoint.y);
			buffer.writeDouble(specificPoint.z);
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null)
				return;
			Entity entityByID = sender.level()
				.getEntity(target);
			if (entityByID != null && ExtendoGripItem.isHoldingExtendoGrip(sender)) {
				double d = sender.getAttribute(ForgeMod.BLOCK_REACH.get())
					.getValue();
				if (!sender.hasLineOfSight(entityByID))
					d -= 3;
				d *= d;
				if (sender.distanceToSqr(entityByID) > d)
					return;
				if (interactionHand == null)
					sender.attack(entityByID);
				else if (specificPoint == null)
					sender.interactOn(entityByID, interactionHand);
				else
					entityByID.interactAt(sender, specificPoint, interactionHand);
			}
		});
		return true;
	}

}
