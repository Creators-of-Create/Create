package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ClientMotionPacket extends SimplePacketBase {

	private Vec3d motion;
	private boolean onGround;

	public ClientMotionPacket(Vec3d motion, boolean onGround) {
		this.motion = motion;
		this.onGround = onGround;
	}

	public ClientMotionPacket(PacketBuffer buffer) {
		motion = new Vec3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		onGround = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeBoolean(onGround);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null)
					return;
				sender.setMotion(motion);
				sender.onGround = onGround;
				if (onGround) {
					sender.handleFallDamage(sender.fallDistance, 1);
					sender.fallDistance = 0;
					sender.connection.floatingTickCount = 0;
				}
			});
		context.get()
			.setPacketHandled(true);
	}

}
