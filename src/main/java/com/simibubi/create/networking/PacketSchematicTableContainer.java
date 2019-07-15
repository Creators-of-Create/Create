package com.simibubi.create.networking;

import java.util.function.Supplier;

import com.simibubi.create.block.SchematicTableContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSchematicTableContainer {

	public String schematic;
	public float progress;

	public PacketSchematicTableContainer(String schematicToUpload, float progress) {
		this.schematic = schematicToUpload;
		if (this.schematic == null)
			this.schematic = "";
		this.progress = progress;
	}

	public PacketSchematicTableContainer(PacketBuffer buffer) {
		this.schematic = buffer.readString();
		this.progress = buffer.readFloat();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeString(schematic);
		buffer.writeFloat(progress);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			Container c = Minecraft.getInstance().player.openContainer;
			if (c != null && c instanceof SchematicTableContainer) {
				((SchematicTableContainer) c).receiveSchematicInfo(schematic, progress);
			}
		});
		
	}
	
}
