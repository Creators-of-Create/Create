package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class LimbSwingUpdatePacket extends SimplePacketBase {

	private int entityId;
	private Vec3d position;
	private float limbSwing;

	public LimbSwingUpdatePacket(int entityId, Vec3d position, float limbSwing) {
		this.entityId = entityId;
		this.position = position;
		this.limbSwing = limbSwing;
	}

	public LimbSwingUpdatePacket(PacketBuffer buffer) {
		entityId = buffer.readInt();
		position = new Vec3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(entityId);
		buffer.writeFloat((float) position.x);
		buffer.writeFloat((float) position.y);
		buffer.writeFloat((float) position.z);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ClientWorld world = Minecraft.getInstance().world;
				if (world == null)
					return;
				Entity entity = world.getEntityByID(entityId);
				if (entity == null)
					return;
				CompoundNBT data = entity.getPersistentData();
				data.putInt("LastOverrideLimbSwingUpdate", 0);
				data.putFloat("OverrideLimbSwing", limbSwing);
				entity.setPositionAndRotationDirect(position.x, position.y, position.z, entity.rotationYaw,
					entity.rotationPitch, 2, false);
			});
		context.get()
			.setPacketHandled(true);
	}

}
