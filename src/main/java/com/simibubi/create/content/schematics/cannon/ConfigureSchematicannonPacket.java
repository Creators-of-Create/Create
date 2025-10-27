package com.simibubi.create.content.schematics.cannon;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ConfigureSchematicannonPacket(Option option, boolean set) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ConfigureSchematicannonPacket> STREAM_CODEC = StreamCodec.composite(
			Option.STREAM_CODEC, ConfigureSchematicannonPacket::option,
			ByteBufCodecs.BOOL, ConfigureSchematicannonPacket::set,
			ConfigureSchematicannonPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_SCHEMATICANNON;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (player == null || !(player.containerMenu instanceof SchematicannonMenu))
			return;

		SchematicannonBlockEntity be = ((SchematicannonMenu) player.containerMenu).contentHolder;
		switch (this.option) {
			case DONT_REPLACE:
			case REPLACE_ANY:
			case REPLACE_EMPTY:
			case REPLACE_SOLID:
				be.replaceMode = this.option.ordinal();
				break;
			case SKIP_MISSING:
				be.skipMissing = this.set;
				break;
			case SKIP_BLOCK_ENTITIES:
				be.replaceBlockEntities = this.set;
				break;

			case PLAY:
				be.state = State.RUNNING;
				be.statusMsg = "running";
				break;
			case PAUSE:
				be.state = State.PAUSED;
				be.statusMsg = "paused";
				break;
			case STOP:
				be.state = State.STOPPED;
				be.statusMsg = "stopped";
				break;
			default:
				break;
		}

		be.sendUpdate = true;
	}

	public enum Option {
		DONT_REPLACE, REPLACE_SOLID, REPLACE_ANY, REPLACE_EMPTY, SKIP_MISSING, SKIP_BLOCK_ENTITIES, PLAY, PAUSE, STOP;

		public static final StreamCodec<ByteBuf, Option> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Option.class);
	}
}
