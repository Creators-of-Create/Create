package com.simibubi.create.networking;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.block.SchematicTableContainer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSchematicUpload {

	public static final int BEGIN = 0;
	public static final int WRITE = 1;
	public static final int FINISH = 2;

	private int code;
	private String schematic;
	private byte[] data;

	public PacketSchematicUpload(int code, String schematic) {
		this.code = code;
		this.schematic = schematic;
	}

	public static PacketSchematicUpload begin(String schematic) {
		PacketSchematicUpload pkt = new PacketSchematicUpload(BEGIN, schematic);
		return pkt;
	}

	public static PacketSchematicUpload write(String schematic, byte[] data) {
		PacketSchematicUpload pkt = new PacketSchematicUpload(WRITE, schematic);
		pkt.data = data;
		return pkt;
	}

	public static PacketSchematicUpload finish(String schematic) {
		return new PacketSchematicUpload(FINISH, schematic);
	}

	public PacketSchematicUpload(PacketBuffer buffer) {
		code = buffer.readInt();
		schematic = buffer.readString();

		if (code == WRITE)
			data = buffer.readByteArray();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeInt(code);
		buffer.writeString(schematic);

		if (code == WRITE)
			buffer.writeByteArray(data);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (code == BEGIN) {
				BlockPos pos = ((SchematicTableContainer) player.openContainer).getTileEntity().getPos();
				Create.sSchematicLoader.handleNewUpload(player, schematic, new DimensionPos(player, pos));
			}
			if (code == WRITE) {
				Create.sSchematicLoader.handleWriteRequest(player, schematic, data);
			}
			if (code == FINISH) {
				Create.sSchematicLoader.handleFinishedUpload(player, schematic);
			}
		});
	}

	public static class DimensionPos {
		public ServerWorld world;
		public BlockPos pos;

		public DimensionPos(ServerPlayerEntity player, BlockPos pos) {
			this.world = player.getServerWorld();
			this.pos = pos;
		}
	}

}
