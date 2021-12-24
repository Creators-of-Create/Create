package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;


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
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		transform.writeToBuffer(buffer);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT,
				() -> () -> AbstractContraptionEntity.handleDisassemblyPacket(this)));
		context.get()
			.setPacketHandled(true);
	}

}
