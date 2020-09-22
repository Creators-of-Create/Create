package com.simibubi.create.content.curiosities.zapper;

import java.util.function.Supplier;

import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler.LaserBeam;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ZapperBeamPacket extends SimplePacketBase {

	public Vector3d start;
	public Vector3d target;
	public Hand hand;
	public boolean self;

	public ZapperBeamPacket(Vector3d start, Vector3d target, Hand hand, boolean self) {
		this.start = start;
		this.target = target;
		this.hand = hand;
		this.self = self;
	}
	
	public ZapperBeamPacket(PacketBuffer buffer) {
		start = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		target = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		hand = buffer.readBoolean()? Hand.MAIN_HAND : Hand.OFF_HAND;
		self = buffer.readBoolean();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeDouble(start.x);
		buffer.writeDouble(start.y);
		buffer.writeDouble(start.z);
		buffer.writeDouble(target.x);
		buffer.writeDouble(target.y);
		buffer.writeDouble(target.z);
		
		buffer.writeBoolean(hand == Hand.MAIN_HAND);
		buffer.writeBoolean(self);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if (Minecraft.getInstance().player.getPositionVec().distanceTo(start) > 100)
				return;
			ZapperRenderHandler.addBeam(new LaserBeam(start, target).followPlayer(self, hand == Hand.MAIN_HAND));
			
			if (self)
				ZapperRenderHandler.shoot(hand);
			else
				ZapperRenderHandler.playSound(hand, new BlockPos(start));
		}));
		context.get().setPacketHandled(true);
	}

}
