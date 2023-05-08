package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionSeatMappingPacket extends SimplePacketBase {

	private Map<UUID, Integer> mapping;
	private int entityID;
	private int dismountedID;

	public ContraptionSeatMappingPacket(int entityID, Map<UUID, Integer> mapping) {
		this(entityID, mapping, -1);
	}
	
	public ContraptionSeatMappingPacket(int entityID, Map<UUID, Integer> mapping, int dismountedID) {
		this.entityID = entityID;
		this.mapping = mapping;
		this.dismountedID = dismountedID;
	}

	public ContraptionSeatMappingPacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		dismountedID = buffer.readInt();
		mapping = new HashMap<>();
		short size = buffer.readShort();
		for (int i = 0; i < size; i++)
			mapping.put(buffer.readUUID(), (int) buffer.readShort());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		buffer.writeInt(dismountedID);
		buffer.writeShort(mapping.size());
		mapping.forEach((k, v) -> {
			buffer.writeUUID(k);
			buffer.writeShort(v);
		});
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entityByID = Minecraft.getInstance().level.getEntity(entityID);
			if (!(entityByID instanceof AbstractContraptionEntity))
				return;
			AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
			
			if (dismountedID != -1) {
				Entity dismountedByID = Minecraft.getInstance().level.getEntity(dismountedID);
				if (Minecraft.getInstance().player != dismountedByID)
					return;
				Vec3 transformedVector = contraptionEntity.getPassengerPosition(dismountedByID, 1);
				if (transformedVector != null)
					dismountedByID.getPersistentData()
						.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
			}
			
			contraptionEntity.getContraption()
				.setSeatMapping(mapping);
		});
		return true;
	}

}
