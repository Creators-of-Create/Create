package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionRelocationPacket extends SimplePacketBase {

	int entityID;

	public ContraptionRelocationPacket(int entityID) {
		this.entityID = entityID;
	}

	public ContraptionRelocationPacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> OrientedContraptionEntity.handleRelocationPacket(this)));
		context.get()
			.setPacketHandled(true);
	}

}
