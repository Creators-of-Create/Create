package com.simibubi.create.content.curiosities.weapons;

import javax.annotation.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;

public class PotatoProjectileEntity extends DamagingProjectileEntity implements IEntityAdditionalSpawnData {

	protected PotatoCannonProjectileType type;
	protected ItemStack stack = ItemStack.EMPTY;

	protected Entity stuckEntity;
	protected Vector3d stuckOffset;
	protected PotatoProjectileRenderMode stuckRenderer;
	protected double stuckFallSpeed;

	protected float additionalDamageMult = 1;
	protected float additionalKnockback = 0;
	protected float recoveryChance = 0;

	public PotatoProjectileEntity(EntityType<? extends DamagingProjectileEntity> type, World world) {
		super(type, world);
	}

	public ItemStack getItem() {
		return stack;
	}

	public void setItem(ItemStack stack) {
		this.stack = stack;
	}

	public PotatoCannonProjectileType getProjectileType() {
		if (type == null)
			type = PotatoProjectileTypeManager.getTypeForStack(stack)
				.orElse(BuiltinPotatoProjectileTypes.FALLBACK);
		return type;
	}

	public void setEnchantmentEffectsFromCannon(ItemStack cannon) {
		int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, cannon);
		int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, cannon);
		int flame = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, cannon);
		int recovery = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.POTATO_RECOVERY.get(), cannon);

		if (power > 0)
			additionalDamageMult = 1 + power * .2f;
		if (punch > 0)
			additionalKnockback = punch * .5f;
		if (flame > 0)
			setSecondsOnFire(100);
		if (recovery > 0)
			recoveryChance = .125f + recovery * .125f;
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT nbt) {
		stack = ItemStack.of(nbt.getCompound("Item"));
		additionalDamageMult = nbt.getFloat("AdditionalDamage");
		additionalKnockback = nbt.getFloat("AdditionalKnockback");
		recoveryChance = nbt.getFloat("Recovery");
		super.readAdditionalSaveData(nbt);
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT nbt) {
		nbt.put("Item", stack.serializeNBT());
		nbt.putFloat("AdditionalDamage", additionalDamageMult);
		nbt.putFloat("AdditionalKnockback", additionalKnockback);
		nbt.putFloat("Recovery", recoveryChance);
		super.addAdditionalSaveData(nbt);
	}

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
		this.stuckRenderer = new PotatoProjectileRenderMode.StuckToEntity(stuckOffset);
		this.stuckFallSpeed = 0.0;
		setDeltaMovement(Vector3d.ZERO);
	}

	public PotatoProjectileRenderMode getRenderMode() {
		if (getStuckEntity() != null)
			return stuckRenderer;

		return getProjectileType().getRenderMode();
	}

	public void tick() {
		PotatoCannonProjectileType projectileType = getProjectileType();

		Entity stuckEntity = getStuckEntity();
		if (stuckEntity != null) {
			if (getY() < stuckEntity.getY() - 0.1) {
				pop(position());
				remove();
			} else {
				stuckFallSpeed += 0.007 * projectileType.getGravityMultiplier();
				stuckOffset = stuckOffset.add(0, -stuckFallSpeed, 0);
				Vector3d pos = stuckEntity.position()
					.add(stuckOffset);
				setPos(pos.x, pos.y, pos.z);
			}
		} else {
			setDeltaMovement(getDeltaMovement().add(0, -0.05 * projectileType.getGravityMultiplier(), 0)
				.scale(projectileType.getDrag()));
		}

		super.tick();
	}

	@Override
	protected float getInertia() {
		return 1;
	}

	@Override
	protected IParticleData getTrailParticle() {
		return new AirParticleData(1, 10);
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	protected void onHitEntity(EntityRayTraceResult ray) {
		super.onHitEntity(ray);

		if (getStuckEntity() != null)
			return;

		Vector3d hit = ray.getLocation();
		Entity target = ray.getEntity();
		PotatoCannonProjectileType projectileType = getProjectileType();
		float damage = projectileType.getDamage() * additionalDamageMult;
		float knockback = projectileType.getKnockback() + additionalKnockback;
		Entity owner = this.getOwner();

		if (!target.isAlive())
			return;
		if (owner instanceof LivingEntity)
			((LivingEntity) owner).setLastHurtMob(target);
		if (target instanceof PotatoProjectileEntity && tickCount < 10 && target.tickCount < 10)
			return;

		pop(hit);

		if (target instanceof WitherEntity && ((WitherEntity) target).isPowered())
			return;
		if (projectileType.preEntityHit(ray))
			return;

		boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;
		int k = target.getRemainingFireTicks();
		if (this.isOnFire() && !targetIsEnderman)
			target.setSecondsOnFire(5);

		boolean onServer = !level.isClientSide;
		if (onServer && !target.hurt(causePotatoDamage(), damage)) {
			target.setRemainingFireTicks(k);
			remove();
			return;
		}

		if (targetIsEnderman)
			return;

		if (!projectileType.onEntityHit(ray) && onServer)
			if (random.nextDouble() <= recoveryChance)
				recoverItem();

		if (!(target instanceof LivingEntity)) {
			playHitSound(level, position());
			remove();
			return;
		}

		LivingEntity livingentity = (LivingEntity) target;

		if (type.getReloadTicks() < 10)
			livingentity.invulnerableTime = type.getReloadTicks() + 10;

		if (onServer && knockback > 0) {
			Vector3d appliedMotion = this.getDeltaMovement()
				.multiply(1.0D, 0.0D, 1.0D)
				.normalize()
				.scale(knockback * 0.6);
			if (appliedMotion.lengthSqr() > 0.0D)
				livingentity.push(appliedMotion.x, 0.1D, appliedMotion.z);
		}

		if (onServer && owner instanceof LivingEntity) {
			EnchantmentHelper.doPostHurtEffects(livingentity, owner);
			EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingentity);
		}

		if (livingentity != owner && livingentity instanceof PlayerEntity && owner instanceof ServerPlayerEntity
			&& !this.isSilent()) {
			((ServerPlayerEntity) owner).connection
				.send(new SChangeGameStatePacket(SChangeGameStatePacket.ARROW_HIT_PLAYER, 0.0F));
		}

		if (onServer && owner instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) owner;
			if (!target.isAlive() && target.getType()
				.getCategory() == EntityClassification.MONSTER
				|| (target instanceof PlayerEntity && target != owner))
				AllTriggers.POTATO_KILL.trigger(serverplayerentity);
		}

		if (type.isSticky() && target.isAlive()) {
			setStuckEntity(target);
		} else {
			remove();
		}

	}

	private void recoverItem() {
		if (!stack.isEmpty())
			spawnAtLocation(ItemHandlerHelper.copyStackWithSize(stack, 1));
	}

	public static void playHitSound(World world, Vector3d location) {
		AllSoundEvents.POTATO_HIT.playOnServer(world, new BlockPos(location));
	}

	public static void playLaunchSound(World world, Vector3d location, float pitch) {
		AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
	}

	@Override
	protected void onHitBlock(BlockRayTraceResult ray) {
		Vector3d hit = ray.getLocation();
		pop(hit);
		if (!getProjectileType().onBlockHit(level, ray) && !level.isClientSide)
			if (random.nextDouble() <= recoveryChance)
				recoverItem();
		super.onHitBlock(ray);
		remove();
	}

	@Override
	public boolean hurt(DamageSource source, float amt) {
		if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE)
			return false;
		if (this.isInvulnerableTo(source))
			return false;
		pop(position());
		remove();
		return true;
	}

	private void pop(Vector3d hit) {
		if (!stack.isEmpty()) {
			for (int i = 0; i < 7; i++) {
				Vector3d m = VecHelper.offsetRandomly(Vector3d.ZERO, this.random, .25f);
				level.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y, m.z);
			}
		}
		if (!level.isClientSide)
			playHitSound(level, position());
	}

	private DamageSource causePotatoDamage() {
		return new PotatoDamageSource(this, getOwner()).setProjectile();
	}

	public static class PotatoDamageSource extends IndirectEntityDamageSource {

		public PotatoDamageSource(Entity source, @Nullable Entity trueSource) {
			super("create.potato_cannon", source, trueSource);
		}

	}

	@SuppressWarnings("unchecked")
	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		EntityType.Builder<PotatoProjectileEntity> entityBuilder = (EntityType.Builder<PotatoProjectileEntity>) builder;
		return entityBuilder.sized(.25f, .25f);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
	}

}
