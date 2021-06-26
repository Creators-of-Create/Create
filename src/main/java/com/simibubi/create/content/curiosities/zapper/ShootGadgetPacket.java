package com.simibubi.create.content.curiosities.zapper;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class ShootGadgetPacket extends SimplePacketBase {

	public Vector3d location;
	public Hand hand;
	public boolean self;

	public ShootGadgetPacket(Vector3d location, Hand hand, boolean self) {
		this.location = location;
		this.hand = hand;
		this.self = self;
	}

	public ShootGadgetPacket(PacketBuffer buffer) {
		hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		self = buffer.readBoolean();
		location = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		readAdditional(buffer);
	}

	public final void write(PacketBuffer buffer) {
		buffer.writeBoolean(hand == Hand.MAIN_HAND);
		buffer.writeBoolean(self);
		buffer.writeDouble(location.x);
		buffer.writeDouble(location.y);
		buffer.writeDouble(location.z);
		writeAdditional(buffer);
	}

	protected abstract void readAdditional(PacketBuffer buffer);

	protected abstract void writeAdditional(PacketBuffer buffer);
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void handleAdditional();

	@OnlyIn(Dist.CLIENT)
	protected abstract ShootableGadgetRenderHandler getHandler();
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public final void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				Entity renderViewEntity = Minecraft.getInstance()
					.getRenderViewEntity();
				if (renderViewEntity == null)
					return;
				if (renderViewEntity.getPositionVec()
					.distanceTo(location) > 100)
					return;
				
				ShootableGadgetRenderHandler handler = getHandler();
				handleAdditional();
				if (self)
					handler.shoot(hand);
				else
					handler.playSound(hand, new BlockPos(location));
			});
		context.get()
			.setPacketHandled(true);
	}

}
