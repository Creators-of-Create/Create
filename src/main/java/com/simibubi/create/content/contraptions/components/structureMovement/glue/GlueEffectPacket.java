package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class GlueEffectPacket extends SimplePacketBase {

	private BlockPos pos;
	private Direction direction;
	private boolean fullBlock;

	public GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) {
		this.pos = pos;
		this.direction = direction;
		this.fullBlock = fullBlock;
	}

	public GlueEffectPacket(PacketBuffer buffer) {
		pos = buffer.readBlockPos();
		direction = Direction.from3DDataValue(buffer.readByte());
		fullBlock = buffer.readBoolean();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeByte(direction.get3DDataValue());
		buffer.writeBoolean(fullBlock);
	}

	@OnlyIn(Dist.CLIENT)
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			Minecraft mc = Minecraft.getInstance();
			if (!mc.player.blockPosition().closerThan(pos, 100))
				return;
			SuperGlueItem.spawnParticles(mc.level, pos, direction, fullBlock);
		}));
		context.get().setPacketHandled(true);
	}

}
