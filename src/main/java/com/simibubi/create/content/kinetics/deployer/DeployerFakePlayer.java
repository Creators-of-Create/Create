package com.simibubi.create.content.kinetics.deployer;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.UsernameCache;
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
	public static final UUID fallbackID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemStack spawnedItemEffects;
	public boolean placedTracks;
	public boolean onMinecartContraption;
	private UUID owner;

	public DeployerFakePlayer(ServerLevel world, @Nullable UUID owner) {
		super(world, new DeployerGameProfile(fallbackID, "Deployer", owner));
		connection = new FakePlayNetHandler(world.getServer(), this);
		this.owner = owner;
	}

	@Override
	public OptionalInt openMenu(MenuProvider menuProvider) {
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

	@Override
	public boolean canBeAffected(MobEffectInstance pEffectInstance) {
		return false;
	}

	@Override
	public UUID getUUID() {
		return owner == null ? super.getUUID() : owner;
	}

	@SubscribeEvent
	public static void deployerHasEyesOnHisFeet(EntityEvent.Size event) {
		if (event.getEntity() instanceof DeployerFakePlayer)
			event.setNewEyeHeight(0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void deployerCollectsDropsFromKilledEntities(LivingDropsEvent event) {
		DamageSource source = event.getSource();
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
	protected boolean doesEmitEquipEvent(EquipmentSlot p_217035_) {
		return false;
	}

	@Override
	public void remove(RemovalReason p_150097_) {
		if (blockBreakingProgress != null && !level().isClientSide)
			level().destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
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
		LivingEntity entityLiving = event.getEntity();
		if (!(entityLiving instanceof Mob))
			return;
		Mob mob = (Mob) entityLiving;

		CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();

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

	// Credit to Mekanism for this approach. Helps fake players get past claims and
	// protection by other mods
	private static class DeployerGameProfile extends GameProfile {

		private UUID owner;

		public DeployerGameProfile(UUID id, String name, UUID owner) {
			super(id, name);
			this.owner = owner;
		}

		@Override
		public UUID getId() {
			return owner == null ? super.getId() : owner;
		}

		@Override
		public String getName() {
			if (owner == null)
				return super.getName();
			String lastKnownUsername = UsernameCache.getLastKnownUsername(owner);
			return lastKnownUsername == null ? super.getName() : lastKnownUsername;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o)
				return true;
			if (!(o instanceof GameProfile otherProfile))
				return false;
			return Objects.equals(getId(), otherProfile.getId()) && Objects.equals(getName(), otherProfile.getName());
		}

		@Override
		public int hashCode() {
			UUID id = getId();
			String name = getName();
			int result = id == null ? 0 : id.hashCode();
			result = 31 * result + (name == null ? 0 : name.hashCode());
			return result;
		}
	}

	private static class FakePlayNetHandler extends ServerGamePacketListenerImpl {
		public FakePlayNetHandler(MinecraftServer server, ServerPlayer playerIn) {
			super(server, NETWORK_MANAGER, playerIn);
		}

		@Override
		public void send(Packet<?> packetIn) {}

		@Override
		public void send(Packet<?> p_243227_, @Nullable PacketSendListener p_243273_) {}
	}

}
