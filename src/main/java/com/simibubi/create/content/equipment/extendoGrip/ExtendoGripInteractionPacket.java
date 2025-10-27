package com.simibubi.create.content.equipment.extendoGrip;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public record ExtendoGripInteractionPacket(InteractionHand hand, int target, Vec3 point) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ExtendoGripInteractionPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND), ExtendoGripInteractionPacket::hand,
			ByteBufCodecs.INT, ExtendoGripInteractionPacket::target,
			CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.VEC3), ExtendoGripInteractionPacket::point,
	        ExtendoGripInteractionPacket::new
	);

	public ExtendoGripInteractionPacket(Entity target) {
		this(target, null);
	}

	public ExtendoGripInteractionPacket(Entity target, InteractionHand hand) {
		this(target, hand, null);
	}

	public ExtendoGripInteractionPacket(Entity target, InteractionHand hand, Vec3 specificPoint) {
		this(hand, target.getId(), specificPoint);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.EXTENDO_INTERACT;
	}

	@Override
	public void handle(ServerPlayer sender) {
		if (sender == null)
			return;
		Entity entityByID = sender.level()
				.getEntity(this.target);
		if (entityByID != null && ExtendoGripItem.isHoldingExtendoGrip(sender)) {
			double d = sender.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
			if (!sender.hasLineOfSight(entityByID))
				d -= 3;
			d *= d;
			if (sender.distanceToSqr(entityByID) > d)
				return;
			if (this.hand == null)
				sender.attack(entityByID);
			else if (this.point == null)
				sender.interactOn(entityByID, this.hand);
			else
				entityByID.interactAt(sender, this.point, this.hand);
		}
	}
}
