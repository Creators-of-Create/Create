package com.simibubi.create.modules.curiosities.blockzapper;

import java.util.function.Supplier;

import com.simibubi.create.foundation.packet.SimplePacketBase;
import com.simibubi.create.modules.curiosities.blockzapper.BlockzapperHandler.LaserBeam;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class BlockzapperBeamPacket extends SimplePacketBase {

	public Vec3d start;
	public Vec3d target;
	public Hand hand;
	public boolean self;

	public BlockzapperBeamPacket(Vec3d start, Vec3d target, Hand hand, boolean self) {
		this.start = start;
		this.target = target;
		this.hand = hand;
		this.self = self;
	}
	
	public BlockzapperBeamPacket(PacketBuffer buffer) {
		start = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		target = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
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
			if (Minecraft.getInstance().player.getPositionVector().distanceTo(start) > 100)
				return;
			BlockzapperHandler.addBeam(new LaserBeam(start, target).followPlayer(self, hand == Hand.MAIN_HAND));
			
			if (self)
				BlockzapperHandler.shoot(hand);
			else
				BlockzapperHandler.playSound(hand, new BlockPos(start));
		}));
		context.get().setPacketHandled(true);
	}

}
