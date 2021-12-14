package com.simibubi.create.content.contraptions.components.deployer;

import java.util.Collection;
import java.util.OptionalInt;
import java.util.UUID;

import dev.cafeteria.fakeplayerapi.server.FakePlayerBuilder;
import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer;

import net.minecraft.resources.ResourceLocation;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DeployerFakePlayer extends FakeServerPlayer {

	private static final Connection NETWORK_MANAGER = new Connection(PacketFlow.CLIENTBOUND);
	public static final GameProfile DEPLOYER_PROFILE =
		new GameProfile(UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d"), "Deployer");
	public static final FakePlayerBuilder BUILDER = new FakePlayerBuilder(new ResourceLocation("create", "deployer"));
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemStack spawnedItemEffects;

	public DeployerFakePlayer(ServerLevel world) {
		super(BUILDER, world.getServer(), world, DEPLOYER_PROFILE);
		connection = new FakePlayNetHandler(world.getServer(), this);
	}

	@Override
	public OptionalInt openMenu(MenuProvider container) {
		return OptionalInt.empty();
	}

	@Override
	public Component getDisplayName() {
		return Lang.translate("block.deployer.damage_source_name");
	}

	@Override
	@Environment(EnvType.CLIENT)
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

	public static int deployerHasEyesOnHisFeet(Entity entity) {
		if (entity instanceof DeployerFakePlayer)
			return 0;
		return -1;
	}

	public static boolean deployerCollectsDropsFromKilledEntities(DamageSource s, Collection<ItemEntity> drops) {
		if (!(s instanceof EntityDamageSource))
			return false;
		EntityDamageSource source = (EntityDamageSource) s;
		Entity trueSource = source.getEntity();
		if (trueSource != null && trueSource instanceof DeployerFakePlayer) {
			DeployerFakePlayer fakePlayer = (DeployerFakePlayer) trueSource;
			drops
				.forEach(stack -> fakePlayer.getInventory()
					.placeItemBackInInventory(stack.getItem()));
			return true;
		}
		return false;
	}

	@Override
	protected void equipEventAndSound(ItemStack p_147219_) {}

	@Override
	public void remove(RemovalReason p_150097_) {
		if (blockBreakingProgress != null && !level.isClientSide)
			level.destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
		super.remove(p_150097_);
	}

	public static int deployerKillsDoNotSpawnXP(int i, Player player) {
		if (player instanceof DeployerFakePlayer)
			return 0;
		return i;
	}

	public static void entitiesDontRetaliate(LivingEntity entityLiving, LivingEntity target) {
		if (!(target instanceof DeployerFakePlayer))
			return;
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
