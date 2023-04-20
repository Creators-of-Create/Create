package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class ContraptionColliderLockPacket extends SimplePacketBase {

	protected int contraption;
	protected double offset;
	private int sender;

	public ContraptionColliderLockPacket(int contraption, double offset, int sender) {
		this.contraption = contraption;
		this.offset = offset;
		this.sender = sender;
	}

	public ContraptionColliderLockPacket(FriendlyByteBuf buffer) {
		contraption = buffer.readVarInt();
		offset = buffer.readDouble();
		sender = buffer.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(contraption);
		buffer.writeDouble(offset);
		buffer.writeVarInt(sender);
	}

	@Override
	public boolean handle(Context context) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> ContraptionCollider.lockPacketReceived(contraption, sender, offset));
		return true;
	}

	public static class ContraptionColliderLockPacketRequest extends SimplePacketBase {

		protected int contraption;
		protected double offset;
		
		public ContraptionColliderLockPacketRequest(int contraption, double offset) {
			this.contraption = contraption;
			this.offset = offset;
		}

		public ContraptionColliderLockPacketRequest(FriendlyByteBuf buffer) {
			contraption = buffer.readVarInt();
			offset = buffer.readDouble();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeVarInt(contraption);
			buffer.writeDouble(offset);
		}

		@Override
		public boolean handle(Context context) {
			AllPackets.getChannel()
				.send(PacketDistributor.TRACKING_ENTITY.with(context::getSender),
					new ContraptionColliderLockPacket(contraption, offset, context.getSender()
						.getId()));
			return true;
		}

	}

}
