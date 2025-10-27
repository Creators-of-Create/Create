package com.simibubi.create.content.equipment.potatoCannon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.StuckToEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.particle.AirParticleData;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class PotatoProjectileEntity extends AbstractHurtingProjectile implements IEntityWithComplexSpawn {

	protected PotatoCannonProjectileType type;
	protected ItemStack stack = ItemStack.EMPTY;

	protected Entity stuckEntity;
	protected Vec3 stuckOffset;
	protected PotatoProjectileRenderMode stuckRenderer;
	protected double stuckFallSpeed;

	protected float additionalDamageMult = 1;
	protected float additionalKnockback = 0;
	protected float recoveryChance = 0;

	public PotatoProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
		super(type, level);
	}

	public void setItem(ItemStack stack) {
		this.stack = stack;
		type = PotatoCannonProjectileType.getTypeForItem(level().registryAccess(), stack.getItem())
			.orElseGet(() -> level().registryAccess()
				.registryOrThrow(CreateRegistries.POTATO_PROJECTILE_TYPE)
				.getHolderOrThrow(AllPotatoProjectileTypes.FALLBACK))
			.value();
	}

	public void setEnchantmentEffectsFromCannon(ItemStack cannon) {
		Registry<Enchantment> enchantmentRegistry = registryAccess().registryOrThrow(Registries.ENCHANTMENT);

		int recovery = cannon.getEnchantmentLevel(enchantmentRegistry.getHolderOrThrow(AllEnchantments.POTATO_RECOVERY));

		if (recovery > 0)
			recoveryChance = .125f + recovery * .125f;
	}

	public ItemStack getItem() {
		return stack;
	}

	@Nullable
	public PotatoCannonProjectileType getProjectileType() {
		return type;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		setItem(ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("Item")));
		additionalDamageMult = nbt.getFloat("AdditionalDamage");
		additionalKnockback = nbt.getFloat("AdditionalKnockback");
		recoveryChance = nbt.getFloat("Recovery");
		super.readAdditionalSaveData(nbt);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		nbt.put("Item", stack.saveOptional(this.registryAccess()));
		nbt.putFloat("AdditionalDamage", additionalDamageMult);
		nbt.putFloat("AdditionalKnockback", additionalKnockback);
		nbt.putFloat("Recovery", recoveryChance);
		super.addAdditionalSaveData(nbt);
	}

	@Nullable
	public Entity getStuckEntity() {
		if (stuckEntity == null)
			return null;
		if (!stuckEntity.isAlive())
			return null;
		return stuckEntity;
	}

	public void setStuckEntity(Entity stuckEntity) {
		this.stuckEntity = stuckEntity;
		this.stuckOffset = position().subtract(stuckEntity.position());
		this.stuckRenderer = new StuckToEntity(stuckOffset);
		this.stuckFallSpeed = 0.0;
		setDeltaMovement(Vec3.ZERO);
	}

	public PotatoProjectileRenderMode getRenderMode() {
		if (getStuckEntity() != null)
			return stuckRenderer;

		return type.renderMode();
	}

	@Override
	public void tick() {
		Entity stuckEntity = getStuckEntity();
		if (stuckEntity != null) {
			if (getY() < stuckEntity.getY() - 0.1) {
				pop(position());
				kill();
			} else {
				stuckFallSpeed += 0.007 * type.gravityMultiplier();
				stuckOffset = stuckOffset.add(0, -stuckFallSpeed, 0);
				Vec3 pos = stuckEntity.position()
					.add(stuckOffset);
				setPos(pos.x, pos.y, pos.z);
			}
		} else {
			setDeltaMovement(getDeltaMovement().add(0, -0.05 * type.gravityMultiplier(), 0)
				.scale(type.drag()));
		}

		super.tick();
	}

	@Override
	protected float getInertia() {
		return 1;
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return new AirParticleData(1, 10);
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	protected void onHitEntity(EntityHitResult ray) {
		super.onHitEntity(ray);

		if (getStuckEntity() != null)
			return;

		Vec3 hit = ray.getLocation();
		Entity target = ray.getEntity();
		float damage = type.damage() * additionalDamageMult;
		float knockback = type.knockback() + additionalKnockback;
		Entity owner = this.getOwner();

		if (!target.isAlive())
			return;
		if (owner instanceof LivingEntity)
			((LivingEntity) owner).setLastHurtMob(target);

		if (target instanceof PotatoProjectileEntity ppe) {
			if (tickCount < 10 && target.tickCount < 10)
				return;
			if (ppe.getProjectileType() != getProjectileType()) {
				if (owner instanceof Player p)
					AllAdvancements.POTATO_CANNON_COLLIDE.awardTo(p);
				if (ppe.getOwner() instanceof Player p)
					AllAdvancements.POTATO_CANNON_COLLIDE.awardTo(p);
			}
		}

		pop(hit);

		if (target instanceof WitherBoss && ((WitherBoss) target).isPowered())
			return;
		if (type.preEntityHit(stack, ray))
			return;

		boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;
		int k = target.getRemainingFireTicks();
		if (this.isOnFire() && !targetIsEnderman)
			target.igniteForSeconds(5);

		boolean onServer = !level().isClientSide;
		DamageSource damageSource = causePotatoDamage();
		if (onServer && !target.hurt(damageSource, damage)) {
			target.setRemainingFireTicks(k);
			kill();
			return;
		}

		if (targetIsEnderman)
			return;

		if (!type.onEntityHit(stack, ray) && onServer) {
			if (random.nextDouble() <= recoveryChance) {
				recoverItem();
			} else {
				spawnAtLocation(type.dropStack());
			}
		}

		if (!(target instanceof LivingEntity livingentity)) {
			playHitSound(level(), position());
			kill();
			return;
		}

		if (type.reloadTicks() < 10)
			livingentity.invulnerableTime = type.reloadTicks() + 10;

		if (onServer && knockback > 0) {
			Vec3 appliedMotion = this.getDeltaMovement()
				.multiply(1.0D, 0.0D, 1.0D)
				.normalize();
			if (appliedMotion.lengthSqr() > 0.0D)
				livingentity.knockback(knockback * 0.6, -appliedMotion.x, -appliedMotion.z);
		}

		if (onServer && owner instanceof LivingEntity) {
			EnchantmentHelper.doPostAttackEffects((ServerLevel) level(), livingentity, damageSource);
		}

		if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer
			&& !this.isSilent()) {
			((ServerPlayer) owner).connection
				.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
		}

		if (onServer && owner instanceof ServerPlayer serverplayerentity) {
			if (!target.isAlive() && target.getType()
				.getCategory() == MobCategory.MONSTER || (target instanceof Player && target != owner))
				AllAdvancements.POTATO_CANNON.awardTo(serverplayerentity);
		}

		if (type.sticky() && target.isAlive()) {
			setStuckEntity(target);
		} else {
			kill();
		}

	}

	private void recoverItem() {
		if (!stack.isEmpty())
			spawnAtLocation(stack.copyWithCount(1));
	}

	public static void playHitSound(Level world, Vec3 location) {
		AllSoundEvents.POTATO_HIT.playOnServer(world, BlockPos.containing(location));
	}

	public static void playLaunchSound(Level world, Vec3 location, float pitch) {
		AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
	}

	@Override
	protected void onHitBlock(BlockHitResult ray) {
		Vec3 hit = ray.getLocation();
		pop(hit);
		if (!type.onBlockHit(level(), stack, ray) && !level().isClientSide) {
			if (random.nextDouble() <= recoveryChance) {
				recoverItem();
			} else {
				spawnAtLocation(getProjectileType().dropStack());
			}
		}

		super.onHitBlock(ray);
		kill();
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amt) {
		if (source.is(DamageTypeTags.IS_FIRE))
			return false;
		if (this.isInvulnerableTo(source))
			return false;
		pop(position());
		kill();
		return true;
	}

	private void pop(Vec3 hit) {
		if (!stack.isEmpty()) {
			for (int i = 0; i < 7; i++) {
				Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, this.random, .25f);
				level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y,
					m.z);
			}
		}
		if (!level().isClientSide)
			playHitSound(level(), position());
	}

	private DamageSource causePotatoDamage() {
		return CreateDamageSources.potatoCannon(level(), this, getOwner());
	}

	@SuppressWarnings("unchecked")
	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		EntityType.Builder<PotatoProjectileEntity> entityBuilder = (EntityType.Builder<PotatoProjectileEntity>) builder;
		return entityBuilder.sized(.25f, .25f);
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
	}

}
