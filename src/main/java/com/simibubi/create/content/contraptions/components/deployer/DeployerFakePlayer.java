package com.simibubi.create.content.contraptions.components.deployer;

import java.util.OptionalInt;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Lang;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DeployerFakePlayer extends FakePlayer {

	private static final Connection NETWORK_MANAGER = new Connection(PacketFlow.CLIENTBOUND);
	public static final GameProfile DEPLOYER_PROFILE =
		new GameProfile(UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d"), "Deployer");
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemStack spawnedItemEffects;
	public boolean placedTracks;
	public boolean onMinecartContraption;

	public DeployerFakePlayer(ServerLevel world) {
		super(world, DEPLOYER_PROFILE);
		connection = new FakePlayNetHandler(world.getServer(), this);
	}

	@Override
	public OptionalInt openMenu(MenuProvider container) {
		return OptionalInt.empty();
	}

	@Override
	public Component getDisplayName() {
		return Lang.translateDirect("block.deployer.damage_source_name");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getEyeHeight(Pose poseIn) {
		return 0;
	}

	@Override
	public Vec3 position() {
		return new Vec3(getX(), getY(), getZ());
	}

	@Override
	public float getCurrentItemAttackStrengthDelay() {
		return 1 / 64f;
	}

	@Override
	public boolean canEat(boolean ignoreHunger) {
		return false;
	}

	@Override
	public ItemStack eat(Level world, ItemStack stack) {
		stack.shrink(1);
		return stack;
	}

	@SubscribeEvent
	public static void deployerHasEyesOnHisFeet(EntityEvent.Size event) {
		if (event.getEntity() instanceof DeployerFakePlayer)
			event.setNewEyeHeight(0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void deployerCollectsDropsFromKilledEntities(LivingDropsEvent event) {
		if (!(event.getSource() instanceof EntityDamageSource))
			return;
		EntityDamageSource source = (EntityDamageSource) event.getSource();
		Entity trueSource = source.getEntity();
		if (trueSource != null && trueSource instanceof DeployerFakePlayer) {
			DeployerFakePlayer fakePlayer = (DeployerFakePlayer) trueSource;
			event.getDrops()
				.forEach(stack -> fakePlayer.getInventory()
					.placeItemBackInInventory(stack.getItem()));
			event.setCanceled(true);
		}
	}

	@Override
	protected void equipEventAndSound(ItemStack p_147219_) {}

	@Override
	public void remove(RemovalReason p_150097_) {
		if (blockBreakingProgress != null && !level.isClientSide)
			level.destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
		super.remove(p_150097_);
	}

	@SubscribeEvent
	public static void deployerKillsDoNotSpawnXP(LivingExperienceDropEvent event) {
		if (event.getAttackingPlayer() instanceof DeployerFakePlayer)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void entitiesDontRetaliate(LivingSetAttackTargetEvent event) {
		if (!(event.getTarget() instanceof DeployerFakePlayer))
			return;
		LivingEntity entityLiving = event.getEntityLiving();
		if (!(entityLiving instanceof Mob))
			return;
		Mob mob = (Mob) entityLiving;

		CKinetics.DeployerAggroSetting setting = AllConfigs.SERVER.kinetics.ignoreDeployerAttacks.get();

		switch (setting) {
		case ALL:
			mob.setTarget(null);
			break;
		case CREEPERS:
			if (mob instanceof Creeper)
				mob.setTarget(null);
			break;
		case NONE:
		default:
		}
	}

	private static class FakePlayNetHandler extends ServerGamePacketListenerImpl {
		public FakePlayNetHandler(MinecraftServer server, ServerPlayer playerIn) {
			super(server, NETWORK_MANAGER, playerIn);
		}

		@Override
		public void send(Packet<?> packetIn) {}

		@Override
		public void send(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> futureListeners) {}
	}

}
