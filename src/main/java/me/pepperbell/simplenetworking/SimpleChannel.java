package me.pepperbell.simplenetworking;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
	private final Map<Class<? extends C2SPacket>, Integer> c2sIdMap = new HashMap<>();
	private final Map<Class<? extends S2CPacket>, Integer> s2cIdMap = new HashMap<>();
	private final Int2ObjectMap<Constructor<? extends C2SPacket>> c2sCtorMap = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Constructor<? extends S2CPacket>> s2cCtorMap = new Int2ObjectOpenHashMap<>();
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
		try {
			Constructor<T> ctor = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
			ctor.setAccessible(true);
			c2sCtorMap.put(id, ctor);
			c2sIdMap.put(clazz, id);
		} catch (Exception e) {
			LOGGER.error("Could not register C2S packet for channel '" + channelName + "' with id " + id, e);
		}
	}

	/**
	 * The registered class <b>must</b> have a constructor accepting a {@link FriendlyByteBuf} or else an error will be thrown.
	 * The visibility of this constructor does not matter.
	 */
	public <T extends S2CPacket> void registerS2CPacket(Class<T> clazz, int id) {
		try {
			Constructor<T> ctor = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
			ctor.setAccessible(true);
			s2cCtorMap.put(id, ctor);
			s2cIdMap.put(clazz, id);
		} catch (Exception e) {
			LOGGER.error("Could not register S2C packet for channel '" + channelName + "' with id " + id, e);
		}
	}

	@Nullable
	public FriendlyByteBuf createBuf(C2SPacket packet) {
		Integer id = c2sIdMap.get(packet.getClass());
		if (id == null) {
			LOGGER.error("Could not get id for C2S packet '" + packet.toString() + "' in channel '" + channelName + "'");
			return null;
		}
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		packet.encode(buf);
		return buf;
	}

	@Nullable
	public FriendlyByteBuf createBuf(S2CPacket packet) {
		Integer id = s2cIdMap.get(packet.getClass());
		if (id == null) {
			LOGGER.error("Could not get id for S2C packet '" + packet.toString() + "' in channel '" + channelName + "'");
			return null;
		}
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		packet.encode(buf);
		return buf;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Packet<?> createVanillaPacket(C2SPacket packet) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return null;
		return ClientPlayNetworking.createC2SPacket(channelName, buf);
	}

	@Nullable
	public Packet<?> createVanillaPacket(S2CPacket packet) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return null;
		return ServerPlayNetworking.createS2CPacket(channelName, buf);
	}

	public void send(C2SPacket packet, PacketSender packetSender) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		packetSender.sendPacket(channelName, buf);
	}

	public void send(S2CPacket packet, PacketSender packetSender) {
		FriendlyByteBuf buf = createBuf(packet);
		if (buf == null) return;
		packetSender.sendPacket(channelName, buf);
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

	public void sendToClients(S2CPacket packet, Iterable<ServerPlayer> players) {
		Packet<?> vanillaPacket = createVanillaPacket(packet);
		if (vanillaPacket == null) return;
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
		Collection<ServerPlayer> clients = PlayerLookup.tracking(entity);
		if (entity instanceof ServerPlayer player && !clients.contains(player)) {
			clients = new ArrayList<>(clients);
			clients.add(player);
		}
		sendToClients(packet, clients);
	}

	public void sendToClientsAround(S2CPacket packet, ServerLevel world, Vec3 pos, double radius) {
		sendToClients(packet, PlayerLookup.around(world, pos, radius));
	}

	public void sendToClientsAround(S2CPacket packet, ServerLevel world, Vec3i pos, double radius) {
		sendToClients(packet, PlayerLookup.around(world, pos, radius));
	}

	public ResourceLocation getChannelName() {
		return channelName;
	}

	private class C2SHandler implements ServerPlayNetworking.PlayChannelHandler {
		@Override
		public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			int id = buf.readVarInt();
			C2SPacket packet;
			try {
				packet = c2sCtorMap.get(id).newInstance(buf);
			} catch (Exception e) {
				LOGGER.error("Could not create C2S packet in channel '" + channelName + "' with id " + id, e);
				return;
			}
			packet.handle(server, player, handler, responseSender, SimpleChannel.this);
		}
	}

	@Environment(EnvType.CLIENT)
	private class S2CHandler implements ClientPlayNetworking.PlayChannelHandler {
		@Override
		public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			int id = buf.readVarInt();
			S2CPacket packet;
			try {
				packet = s2cCtorMap.get(id).newInstance(buf);
			} catch (Exception e) {
				LOGGER.error("Could not create S2C packet in channel '" + channelName + "' with id " + id, e);
				return;
			}
			packet.handle(client, handler, responseSender, SimpleChannel.this);
		}
	}
}
