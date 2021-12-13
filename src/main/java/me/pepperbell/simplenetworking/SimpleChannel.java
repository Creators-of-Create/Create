package me.pepperbell.simplenetworking;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class SimpleChannel {
	private static final Logger LOGGER = LogManager.getLogger("Simple Networking API");

	private final ResourceLocation channelName;
	private final BiMap<Integer, Class<?>> c2sIdMap = HashBiMap.create();
	private final BiMap<Integer, Class<?>> s2cIdMap = HashBiMap.create();
	private C2SHandler c2sHandler;
	private S2CHandler s2cHandler;

	public SimpleChannel(ResourceLocation channelName) {
		this.channelName = channelName;
	}

	public void initServerListener() {
		c2sHandler = new C2SHandler();
		ServerPlayNetworking.registerGlobalReceiver(channelName, c2sHandler);
	}

	@Environment(EnvType.CLIENT)
	public void initClientListener() {
		s2cHandler = new S2CHandler();
		ClientPlayNetworking.registerGlobalReceiver(channelName, s2cHandler);
	}

	/**
	 * The registered class <b>must</b> have a constructor accepting a {@link FriendlyByteBuf} or else an error will be thrown.
	 * The visibility of this constructor does not matter.
	 */
	public <T extends C2SPacket> void registerC2SPacket(Class<T> clazz, int id) {
		c2sIdMap.put(id, clazz);
	}

	/**
	 * The registered class <b>must</b> have a constructor accepting a {@link FriendlyByteBuf} or else an error will be thrown.
	 * The visibility of this constructor does not matter.
	 */
	public <T extends S2CPacket> void registerS2CPacket(Class<T> clazz, int id) {
		s2cIdMap.put(id, clazz);
	}

	private FriendlyByteBuf createBuf(C2SPacket packet) {
		Integer id = c2sIdMap.inverse().get(packet.getClass());
		if (id == null) {
			LOGGER.error("Could not get id for C2S packet '" + packet.toString() + "' in channel '" + channelName + "'");
			return null;
		}
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		packet.encode(buf);
		return buf;
	}

	private FriendlyByteBuf createBuf(S2CPacket packet) {
		Integer id = s2cIdMap.inverse().get(packet.getClass());
		if (id == null) {
			LOGGER.error("Could not get id for S2C packet '" + packet.toString() + "' in channel '" + channelName + "'");
			return null;
		}
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		packet.encode(buf);
		return buf;
	}

	@Environment(EnvType.CLIENT)
	public void sendToServer(C2SPacket packet) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		ClientPlayNetworking.send(channelName, buf);
	}

	public void sendToClient(S2CPacket packet, ServerPlayer player) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		ServerPlayNetworking.send(player, channelName, buf);
	}

	public void sendToClients(S2CPacket packet, Collection<ServerPlayer> players) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		Packet<?> vanillaPacket = ServerPlayNetworking.createS2CPacket(channelName, buf);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.getSender(player).sendPacket(vanillaPacket);
		}
	}

	public void sendToClientsInServer(S2CPacket packet, MinecraftServer server) {
		sendToClients(packet, PlayerLookup.all(server));
	}

	public void sendToClientsInWorld(S2CPacket packet, ServerLevel world) {
		sendToClients(packet, PlayerLookup.world(world));
	}

	public void sendToClientsTracking(S2CPacket packet, ServerLevel world, BlockPos pos) {
		sendToClients(packet, PlayerLookup.tracking(world, pos));
	}

	public void sendToClientsTracking(S2CPacket packet, ServerLevel world, ChunkPos pos) {
		sendToClients(packet, PlayerLookup.tracking(world, pos));
	}

	public void sendToClientsTracking(S2CPacket packet, Entity entity) {
		sendToClients(packet, PlayerLookup.tracking(entity));
	}

	public void sendToClientsTracking(S2CPacket packet, BlockEntity blockEntity) {
		sendToClients(packet, PlayerLookup.tracking(blockEntity));
	}

	public void sendToClientsTrackingAndSelf(S2CPacket packet, Entity entity) {
		Collection<ServerPlayer> clients = new ArrayList<>(PlayerLookup.tracking(entity));
		if (entity instanceof ServerPlayer && !clients.contains(entity)) {
			clients.add((ServerPlayer) entity);
		}
		sendToClients(packet, clients);
	}

	public void sendToClientsAround(S2CPacket packet, ServerLevel world, Vec3 pos, double radius) {
		sendToClients(packet, PlayerLookup.around(world, pos, radius));
	}

	public void sendToClientsAround(S2CPacket packet, ServerLevel world, Vec3i pos, double radius) {
		sendToClients(packet, PlayerLookup.around(world, pos, radius));
	}

	@Environment(EnvType.CLIENT)
	public void sendResponseToServer(ResponseTarget target, C2SPacket packet) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		target.sender.sendPacket(channelName, buf);
	}

	public void sendResponseToClient(ResponseTarget target, S2CPacket packet) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		target.sender.sendPacket(channelName, buf);
	}

	public static class ResponseTarget {
		private final PacketSender sender;

		private ResponseTarget(PacketSender sender) {
			this.sender = sender;
		}
	}

	private class C2SHandler implements ServerPlayNetworking.PlayChannelHandler {
		@Override
		public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			int id = buf.readVarInt();
			C2SPacket packet = null;
			try {
				Class<?> clazz = c2sIdMap.get(id);
				Constructor<?> ctor = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
				ctor.setAccessible(true);
				packet = (C2SPacket) ctor.newInstance(buf);
			} catch (Exception e) {
				LOGGER.error("Could not create C2S packet in channel '" + channelName + "' with id " + id, e);
			}
			if (packet != null) {
				packet.handle(server, player, handler, new ResponseTarget(responseSender));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private class S2CHandler implements ClientPlayNetworking.PlayChannelHandler {
		@Override
		public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			int id = buf.readVarInt();
			S2CPacket packet = null;
			try {
				Class<?> clazz = s2cIdMap.get(id);
				Constructor<?> ctor = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
				ctor.setAccessible(true);
				packet = (S2CPacket) ctor.newInstance(buf);
			} catch (Exception e) {
				LOGGER.error("Could not create S2C packet in channel '" + channelName + "' with id " + id, e);
			}
			if (packet != null) {
				packet.execute(client, handler, new ResponseTarget(responseSender));
			}
		}
	}
}
