package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.SchematicFile;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import java.io.File;

public class SchematicUploadPacket extends SimplePacketBase {

	public static final int BEGIN = 0;
	public static final int WRITE = 1;
	public static final int FINISH = 2;

	private int code;
	private long size;
	private String schematic;
	private byte[] data;

	private String md5Hex;

	public SchematicUploadPacket(int code, String schematic) {
		this.code = code;
		this.schematic = schematic;
	}

	public static SchematicUploadPacket begin(String schematic, long size, String md5Hex) {
		SchematicUploadPacket pkt = new SchematicUploadPacket(BEGIN, schematic);
		pkt.size = size;
		pkt.md5Hex = md5Hex;
		return pkt;
	}

	public static SchematicUploadPacket write(String schematic, byte[] data) {
		SchematicUploadPacket pkt = new SchematicUploadPacket(WRITE, schematic);
		pkt.data = data;
		return pkt;
	}

	public static SchematicUploadPacket finish(String schematic) {
		return new SchematicUploadPacket(FINISH, schematic);
	}

	public SchematicUploadPacket(FriendlyByteBuf buffer) {
		code = buffer.readInt();
		schematic = buffer.readUtf(256);

		if (code == BEGIN) {
			size = buffer.readLong();
			md5Hex = buffer.readUtf(32);
		}
		if (code == WRITE)
			data = buffer.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(code);
		buffer.writeUtf(schematic);

		if (code == BEGIN) {
			buffer.writeLong(size);
			buffer.writeUtf(md5Hex);
		}
		if (code == WRITE)
			buffer.writeByteArray(data);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			if (code == BEGIN) {
				boolean usedLocalFile = false;

				BlockPos pos = ((SchematicTableMenu) player.containerMenu).contentHolder.getBlockPos();

				SchematicFile schematicFile = Create.SCHEMATIC_RECEIVER.getSchematicFileFromSum(md5Hex);

				if (schematicFile != null) {
					String filePath = String.format(
							"%s/%s/%s",
							Create.SCHEMATIC_RECEIVER.getSchematicPath(),
							schematicFile.playerName(),
							schematicFile.schematicName()
					);

					// Check if the file exists
					if (new File(filePath).isFile()) {
						Create.SCHEMATIC_RECEIVER.useLocalFile(player.level(), pos, schematicFile);
						usedLocalFile = true;
					}
				}

				if (!usedLocalFile)
					Create.SCHEMATIC_RECEIVER.handleNewUpload(player, schematic, size, pos);
			}
			if (code == WRITE)
				Create.SCHEMATIC_RECEIVER.handleWriteRequest(player, schematic, data);
			if (code == FINISH)
				Create.SCHEMATIC_RECEIVER.handleFinishedUpload(player, schematic);
		});
		return true;
	}

}
