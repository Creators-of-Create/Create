package com.simibubi.create.foundation.networking;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import me.pepperbell.simplenetworking.SimpleChannel.ResponseTarget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public abstract class SimplePacketBase implements C2SPacket, S2CPacket {

	public abstract void write(FriendlyByteBuf buffer);

	public abstract void handle(Supplier<Context> context);

	@Override
	public final void encode(FriendlyByteBuf buffer) {
		write(buffer);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(Minecraft client, ClientPacketListener handler, ResponseTarget responseTarget) {
		handle(new Context(client, handler, null, responseTarget));
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, ResponseTarget responseTarget) {
		handle(new Context(server, handler, player, responseTarget));
	}

	public enum NetworkDirection {
		PLAY_TO_CLIENT,
		PLAY_TO_SERVER
	}

	public record Context(Executor exec, PacketListener listener, @Nullable ServerPlayer sender, ResponseTarget target) implements Supplier<Context> {
		public void enqueueWork(Runnable runnable) {
			exec().execute(runnable);
		}

		@Nullable
		public ServerPlayer getSender() {
			return sender();
		}

		public NetworkDirection getDirection() {
			return sender() == null ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
		}

		public void setPacketHandled(boolean value) {
		}

		@Override
		public Context get() {
			return this;
		}
	}
}
