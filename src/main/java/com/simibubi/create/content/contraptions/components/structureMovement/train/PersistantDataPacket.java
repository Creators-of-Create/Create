package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PersistantDataPacket extends PersistantDataPacketRequest {

	CompoundNBT persistentData;

	public PersistantDataPacket(Entity entity) {
		super(entity);
		persistentData = entity.getPersistentData();
	}

	public PersistantDataPacket(PacketBuffer buffer) {
		super(buffer);
		persistentData = buffer.readCompoundTag();
	}

	@Override
	public void write(PacketBuffer buffer) {
		super.write(buffer);
		buffer.writeCompoundTag(persistentData);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ClientWorld world = Minecraft.getInstance().world;
				if (world == null)
					return;
				Entity entityByID = world.getEntityByID(entityId);
				if (entityByID == null)
					return;
				CompoundNBT persistentData = entityByID.getPersistentData();
				persistentData.merge(this.persistentData);
				MinecartCouplingHandler.queueLoadedMinecart(entityByID, world);
			});
		context.get()
			.setPacketHandled(true);
	}

}
