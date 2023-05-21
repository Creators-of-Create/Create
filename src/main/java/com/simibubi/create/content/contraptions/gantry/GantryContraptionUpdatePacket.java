package com.simibubi.create.content.contraptions.gantry;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class GantryContraptionUpdatePacket extends SimplePacketBase {

	int entityID;
	double coord;
	double motion;
	double sequenceLimit;

	public GantryContraptionUpdatePacket(int entityID, double coord, double motion, double sequenceLimit) {
		this.entityID = entityID;
		this.coord = coord;
		this.motion = motion;
		this.sequenceLimit = sequenceLimit;
	}

	public GantryContraptionUpdatePacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		coord = buffer.readFloat();
		motion = buffer.readFloat();
		sequenceLimit = buffer.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		buffer.writeFloat((float) coord);
		buffer.writeFloat((float) motion);
		buffer.writeFloat((float) sequenceLimit);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GantryContraptionEntity.handlePacket(this)));
		return true;
	}

}
