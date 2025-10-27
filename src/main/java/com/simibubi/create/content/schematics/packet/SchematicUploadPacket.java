package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;


public record SchematicUploadPacket(int code, long size, String schematic, byte[] data) implements ServerboundPacketPayload {
	public static final int BEGIN = 0;
	public static final int WRITE = 1;
	public static final int FINISH = 2;

	public static final StreamCodec<ByteBuf, SchematicUploadPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SchematicUploadPacket::code,
			ByteBufCodecs.VAR_LONG, SchematicUploadPacket::size,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.stringUtf8(256)), SchematicUploadPacket::schematic,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.byteArray(Integer.MAX_VALUE)), SchematicUploadPacket::data,
			SchematicUploadPacket::new
	);

	public static SchematicUploadPacket begin(String schematic, long size) {
		return new SchematicUploadPacket(BEGIN, size, schematic, null);
	}

	public static SchematicUploadPacket write(String schematic, byte[] data) {
		return new SchematicUploadPacket(WRITE, 0, schematic, data);
	}

	public static SchematicUploadPacket finish(String schematic) {
		return new SchematicUploadPacket(FINISH, 0, schematic, null);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.UPLOAD_SCHEMATIC;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (this.code == BEGIN) {
			BlockPos pos = ((SchematicTableMenu) player.containerMenu).contentHolder
					.getBlockPos();
			Create.SCHEMATIC_RECEIVER.handleNewUpload(player, this.schematic, this.size, pos);
		}
		if (this.code == WRITE)
			Create.SCHEMATIC_RECEIVER.handleWriteRequest(player, this.schematic, this.data);
		if (this.code == FINISH)
			Create.SCHEMATIC_RECEIVER.handleFinishedUpload(player, this.schematic);
	}
}
