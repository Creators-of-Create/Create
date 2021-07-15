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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

	private static final NetworkManager NETWORK_MANAGER = new NetworkManager(PacketDirection.CLIENTBOUND);
	public static final GameProfile DEPLOYER_PROFILE =
		new GameProfile(UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d"), "Deployer");
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemStack spawnedItemEffects;

	public DeployerFakePlayer(ServerWorld world) {
		super(world, DEPLOYER_PROFILE);
		connection = new FakePlayNetHandler(world.getServer(), this);
	}

	@Override
	public OptionalInt openMenu(INamedContainerProvider container) {
		return OptionalInt.empty();
	}

	@Override
	public ITextComponent getDisplayName() {
		return Lang.translate("block.deployer.damage_source_name");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getEyeHeight(Pose poseIn) {
		return 0;
	}

	@Override
	public Vector3d position() {
		return new Vector3d(getX(), getY(), getZ());
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
	public ItemStack eat(World world, ItemStack stack) {
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
				.forEach(stack -> fakePlayer.inventory.placeItemBackInInventory(trueSource.level, stack.getItem()));
			event.setCanceled(true);
		}
	}

	@Override
	protected void playEquipSound(ItemStack p_184606_1_) {}

	@Override
	public void remove(boolean keepData) {
		if (blockBreakingProgress != null && !level.isClientSide)
			level.destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
		super.remove(keepData);
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
		if (!(entityLiving instanceof MobEntity))
			return;
		MobEntity mob = (MobEntity) entityLiving;

		CKinetics.DeployerAggroSetting setting = AllConfigs.SERVER.kinetics.ignoreDeployerAttacks.get();

		switch (setting) {
		case ALL:
			mob.setTarget(null);
			break;
		case CREEPERS:
			if (mob instanceof CreeperEntity)
				mob.setTarget(null);
			break;
		case NONE:
		default:
		}
	}

	private static class FakePlayNetHandler extends ServerPlayNetHandler {
		public FakePlayNetHandler(MinecraftServer server, ServerPlayerEntity playerIn) {
			super(server, NETWORK_MANAGER, playerIn);
		}

		@Override
		public void send(IPacket<?> packetIn) {}

		@Override
		public void send(IPacket<?> packetIn,
			GenericFutureListener<? extends Future<? super Void>> futureListeners) {}
	}

}
