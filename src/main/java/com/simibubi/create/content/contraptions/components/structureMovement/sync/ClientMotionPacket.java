package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class ClientMotionPacket extends SimplePacketBase {

	private Vec3 motion;
	private boolean onGround;
	private float limbSwing;

	public ClientMotionPacket(Vec3 motion, boolean onGround, float limbSwing) {
		this.motion = motion;
		this.onGround = onGround;
		this.limbSwing = limbSwing;
	}

	public ClientMotionPacket(FriendlyByteBuf buffer) {
		motion = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		onGround = buffer.readBoolean();
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeBoolean(onGround);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null)
				return;
			sender.setDeltaMovement(motion);
			sender.setOnGround(onGround);
			if (onGround) {
				sender.causeFallDamage(sender.fallDistance, 1, DamageSource.FALL);
				sender.fallDistance = 0;
				sender.connection.aboveGroundTickCount = 0;
				sender.connection.aboveGroundVehicleTickCount = 0;
			}
			AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> sender),
				new LimbSwingUpdatePacket(sender.getId(), sender.position(), limbSwing));
		});
		return true;
	}

}
