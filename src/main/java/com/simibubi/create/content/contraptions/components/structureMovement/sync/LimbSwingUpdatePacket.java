package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class LimbSwingUpdatePacket extends SimplePacketBase {

	private int entityId;
	private Vec3 position;
	private float limbSwing;

	public LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) {
		this.entityId = entityId;
		this.position = position;
		this.limbSwing = limbSwing;
	}

	public LimbSwingUpdatePacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeDouble(position.x);
		buffer.writeDouble(position.y);
		buffer.writeDouble(position.z);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ClientLevel world = Minecraft.getInstance().level;
			if (world == null)
				return;
			Entity entity = world.getEntity(entityId);
			if (entity == null)
				return;
			CompoundTag data = entity.getPersistentData();
			data.putInt("LastOverrideLimbSwingUpdate", 0);
			data.putFloat("OverrideLimbSwing", limbSwing);
			entity.lerpTo(position.x, position.y, position.z, entity.getYRot(),
				entity.getXRot(), 2, false);
		});
		return true;
	}

}
