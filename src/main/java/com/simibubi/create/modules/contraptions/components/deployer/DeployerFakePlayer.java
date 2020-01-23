package com.simibubi.create.modules.contraptions.components.deployer;

import java.util.OptionalInt;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class DeployerFakePlayer extends FakePlayer {

	private static final NetworkManager NETWORK_MANAGER = new NetworkManager(PacketDirection.CLIENTBOUND);
	public static final GameProfile DEPLOYER_PROFILE = new GameProfile(
			UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d"), "Deployer");

	public DeployerFakePlayer(ServerWorld world) {
		super(world, DEPLOYER_PROFILE);
		connection = new FakePlayNetHandler(world.getServer(), this);
	}

	@Override
	public OptionalInt openContainer(INamedContainerProvider container) {
		return OptionalInt.empty();
	}

	private static class FakePlayNetHandler extends ServerPlayNetHandler {
		public FakePlayNetHandler(MinecraftServer server, ServerPlayerEntity playerIn) {
			super(server, NETWORK_MANAGER, playerIn);
		}

		@Override
		public void sendPacket(IPacket<?> packetIn) {
		}

		@Override
		public void sendPacket(IPacket<?> packetIn,
				GenericFutureListener<? extends Future<? super Void>> futureListeners) {
		}
	}

}
