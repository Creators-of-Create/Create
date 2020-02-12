package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.function.Supplier;

import com.simibubi.create.foundation.packet.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionStallPacket extends SimplePacketBase {

	int entityID;
	float x;
	float y;
	float z;
	float yaw;
	float pitch;
	float roll;

	public ContraptionStallPacket(int entityID, double posX, double posY, double posZ, float yaw, float pitch, float roll) {
		this.entityID = entityID;
		this.x = (float) posX;
		this.y = (float) posY;
		this.z = (float) posZ;
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public ContraptionStallPacket(PacketBuffer buffer) {
		entityID = buffer.readInt();
		x = buffer.readFloat();
		y = buffer.readFloat();
		z = buffer.readFloat();
		yaw = buffer.readFloat();
		pitch = buffer.readFloat();
		roll = buffer.readFloat();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(entityID);
		writeAll(buffer, x, y, z, yaw, pitch, roll);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(
				() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ContraptionEntity.handleStallPacket(this)));
		context.get().setPacketHandled(true);
	}

	private void writeAll(PacketBuffer buffer, float... floats) {
		for (float f : floats)
			buffer.writeFloat(f);
	}

}
