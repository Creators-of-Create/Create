package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.content.schematics.block.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.block.SchematicannonBlockEntity.State;
import com.simibubi.create.content.schematics.block.SchematicannonMenu;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class ConfigureSchematicannonPacket extends SimplePacketBase {

	public static enum Option {
		DONT_REPLACE, REPLACE_SOLID, REPLACE_ANY, REPLACE_EMPTY, SKIP_MISSING, SKIP_BLOCK_ENTITIES, PLAY, PAUSE, STOP;
	}

	private Option option;
	private boolean set;

	public ConfigureSchematicannonPacket(Option option, boolean set) {
		this.option = option;
		this.set = set;
	}

	public ConfigureSchematicannonPacket(FriendlyByteBuf buffer) {
		this(buffer.readEnum(Option.class), buffer.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(option);
		buffer.writeBoolean(set);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null || !(player.containerMenu instanceof SchematicannonMenu))
				return;

			SchematicannonBlockEntity be = ((SchematicannonMenu) player.containerMenu).contentHolder;
			switch (option) {
			case DONT_REPLACE:
			case REPLACE_ANY:
			case REPLACE_EMPTY:
			case REPLACE_SOLID:
				be.replaceMode = option.ordinal();
				break;
			case SKIP_MISSING:
				be.skipMissing = set;
				break;
			case SKIP_BLOCK_ENTITIES:
				be.replaceBlockEntities = set;
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
		});
		return true;
	}

}
