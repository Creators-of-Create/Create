package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.function.Supplier;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;

public class SuperGlueRemovalPacket extends SimplePacketBase {

	private int entityId;
	private BlockPos soundSource;

	public SuperGlueRemovalPacket(int id, BlockPos soundSource) {
		entityId = id;
		this.soundSource = soundSource;
	}

	public SuperGlueRemovalPacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		soundSource = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeBlockPos(soundSource);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			Entity entity = player.level.getEntity(entityId);
			if (!(entity instanceof SuperGlueEntity superGlue))
				return;
			double range = 32;
			if (player.distanceToSqr(superGlue.position()) > range * range)
				return;
			AllSoundEvents.SLIME_ADDED.play(player.level, null, soundSource, 0.5F, 0.5F);
			superGlue.spawnParticles();
			entity.discard();
		});
		ctx.setPacketHandled(true);
	}

}
