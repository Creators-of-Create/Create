package com.simibubi.create.modules.contraptions.components.deployer;

import java.util.OptionalInt;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.utility.Lang;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
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

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(Lang.translate("block.deployer.damage_source_name"));
	}

	@OnlyIn(Dist.CLIENT)
	public float getEyeHeight(Pose poseIn) {
		return 0;
	}

	@Override
	public Vec3d getPositionVector() {
		return new Vec3d(posX, posY, posZ);
	}

	@Override
	public float getCooldownPeriod() {
		return 1 / 64f;
	}

	@SubscribeEvent
	public static void deployerHasEyesOnHisFeet(EntityEvent.EyeHeight event) {
		if (event.getEntity() instanceof DeployerFakePlayer)
			event.setNewHeight(0);
	}
	
	@SubscribeEvent
	public static void deployerCollectsDropsFromKilledEntities(LivingDropsEvent event) {
		if (!(event.getSource() instanceof EntityDamageSource))
			return;
		EntityDamageSource source = (EntityDamageSource) event.getSource();
		Entity trueSource = source.getTrueSource();
		if (trueSource != null && trueSource instanceof DeployerFakePlayer) {
			DeployerFakePlayer fakePlayer = (DeployerFakePlayer) trueSource;
			event.getDrops()
					.forEach(stack -> fakePlayer.inventory.placeItemBackInInventory(trueSource.world, stack.getItem()));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void deployerKillsDoNotSpawnXP(LivingExperienceDropEvent event) {
		if (event.getAttackingPlayer() instanceof DeployerFakePlayer)
			event.setCanceled(true);
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
