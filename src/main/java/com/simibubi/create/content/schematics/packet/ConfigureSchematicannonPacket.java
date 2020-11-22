package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.content.schematics.block.SchematicannonContainer;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.content.schematics.block.SchematicannonTileEntity.State;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ConfigureSchematicannonPacket extends SimplePacketBase {

	public static enum Option {
		DONT_REPLACE, REPLACE_SOLID, REPLACE_ANY, REPLACE_EMPTY, SKIP_MISSING, SKIP_TILES, PLAY, PAUSE, STOP;
	}

	private Option option;
	private boolean set;

	public ConfigureSchematicannonPacket(Option option, boolean set) {
		this.option = option;
		this.set = set;
	}

	public ConfigureSchematicannonPacket(PacketBuffer buffer) {
		this(buffer.readEnumValue(Option.class), buffer.readBoolean());
	}

	public void write(PacketBuffer buffer) {
		buffer.writeEnumValue(option);
		buffer.writeBoolean(set);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null || !(player.openContainer instanceof SchematicannonContainer))
				return;

			SchematicannonTileEntity te = ((SchematicannonContainer) player.openContainer).getTileEntity();
			switch (option) {
			case DONT_REPLACE:
			case REPLACE_ANY:
			case REPLACE_EMPTY:
			case REPLACE_SOLID:
				te.replaceMode = option.ordinal();
				break;
			case SKIP_MISSING:
				te.skipMissing = set;
				break;
			case SKIP_TILES:
				te.replaceTileEntities = set;
				break;

			case PLAY:
				te.state = State.RUNNING;
				te.statusMsg = "running";
				break;
			case PAUSE:
				te.state = State.PAUSED;
				te.statusMsg = "paused";
				break;
			case STOP:
				te.state = State.STOPPED;
				te.statusMsg = "stopped";
				break;
			default:
				break;
			}

			te.sendUpdate = true;
		});
		context.get().setPacketHandled(true);
	}

}
