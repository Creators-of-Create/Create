package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionStallPacket extends SimplePacketBase {

	int entityID;
	float x;
	float y;
	float z;
	float angle;

	public ContraptionStallPacket(int entityID, double posX, double posY, double posZ, float angle) {
		this.entityID = entityID;
		this.x = (float) posX;
		this.y = (float) posY;
		this.z = (float) posZ;
		this.angle = angle;
	}

	public ContraptionStallPacket(PacketBuffer buffer) {
		entityID = buffer.readInt();
		x = buffer.readFloat();
		y = buffer.readFloat();
		z = buffer.readFloat();
		angle = buffer.readFloat();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(entityID);
		writeAll(buffer, x, y, z, angle);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(
				() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> AbstractContraptionEntity.handleStallPacket(this)));
		context.get().setPacketHandled(true);
	}

	private void writeAll(PacketBuffer buffer, float... floats) {
		for (float f : floats)
			buffer.writeFloat(f);
	}

}
