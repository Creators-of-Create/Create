package com.simibubi.create.foundation.networking;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public abstract class SimplePacketBase {

	public abstract void write(FriendlyByteBuf buffer);

	public abstract void handle(Supplier<Context> context);

	public static record Context(Executor exec, PacketListener handler, SimpleChannel.ResponseTarget responseTarget) {
		public void enqueueWork(Runnable runnable) {
			exec().execute(runnable);
		}

		public ServerPlayer getSender() {
			if (handler() instanceof ServerGamePacketListenerImpl serverHandler) {
				return serverHandler.player;
			}
			return null;
		}

		public void setPacketHandled(boolean value) {}
	}
}
