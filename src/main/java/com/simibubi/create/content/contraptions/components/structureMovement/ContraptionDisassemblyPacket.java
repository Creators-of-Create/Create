package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionDisassemblyPacket extends SimplePacketBase {

	int entityID;
	StructureTransform transform;

	public ContraptionDisassemblyPacket(int entityID, StructureTransform transform) {
		this.entityID = entityID;
		this.transform = transform;
	}

	public ContraptionDisassemblyPacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		transform = StructureTransform.fromBuffer(buffer);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		transform.writeToBuffer(buffer);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> AbstractContraptionEntity.handleDisassemblyPacket(this)));
		context.get()
			.setPacketHandled(true);
	}

}
