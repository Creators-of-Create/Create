package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tterrag.registrate.fabric.EnvExecutor;

public class GlueEffectPacket extends SimplePacketBase {

	private BlockPos pos;
	private Direction direction;
	private boolean fullBlock;

	public GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) {
		this.pos = pos;
		this.direction = direction;
		this.fullBlock = fullBlock;
	}

	public GlueEffectPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		direction = Direction.from3DDataValue(buffer.readByte());
		fullBlock = buffer.readBoolean();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeByte(direction.get3DDataValue());
		buffer.writeBoolean(fullBlock);
	}

	@Environment(EnvType.CLIENT)
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
			Minecraft mc = Minecraft.getInstance();
			if (!mc.player.blockPosition().closerThan(pos, 100))
				return;
			SuperGlueItem.spawnParticles(mc.level, pos, direction, fullBlock);
		}));
		context.get().setPacketHandled(true);
	}

}
