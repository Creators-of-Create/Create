package com.simibubi.create.content.curiosities.weapons;

import javax.annotation.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;

public class PotatoProjectileEntity extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData {

	protected PotatoCannonProjectileType type;
	protected ItemStack stack = ItemStack.EMPTY;

	protected Entity stuckEntity;
	protected Vec3 stuckOffset;
	protected PotatoProjectileRenderMode stuckRenderer;
	protected double stuckFallSpeed;

	protected float additionalDamageMult = 1;
	protected float additionalKnockback = 0;
	protected float recoveryChance = 0;

	protected int splitAmount = 0;

	protected int randomIdentifer = 0;

	public PotatoProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level world) {
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

	public void setEnchantmentEffectsFromCannon(ItemStack cannon, int splitAmount) {
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

		this.splitAmount = splitAmount;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		stack = ItemStack.of(nbt.getCompound("Item"));
		additionalDamageMult = nbt.getFloat("AdditionalDamage");
		additionalKnockback = nbt.getFloat("AdditionalKnockback");
		recoveryChance = nbt.getFloat("Recovery");
		super.readAdditionalSaveData(nbt);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
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
		setDeltaMovement(Vec3.ZERO);
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
				kill();
			} else {
				stuckFallSpeed += 0.007 * projectileType.getGravityMultiplier();
				stuckOffset = stuckOffset.add(0, -stuckFallSpeed, 0);
				Vec3 pos = stuckEntity.position()
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
		PotatoCannonProjectileType projectileType = getProjectileType();
		float damage = projectileType.getDamage() * additionalDamageMult;
		float knockback = projectileType.getKnockback() + additionalKnockback;
		Entity owner = this.getOwner();

		if (target instanceof PotatoProjectileEntity)
			if (this.randomIdentifer == ((PotatoProjectileEntity) target).randomIdentifer)
				return;

		split(projectileType);

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
			} else
				return;
		}

		pop(hit);

		if (target instanceof WitherBoss && ((WitherBoss) target).isPowered())
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
			kill();
			return;
		}

		if (targetIsEnderman)
			return;

		if (!projectileType.onEntityHit(ray) && onServer)
			if (random.nextDouble() <= recoveryChance)
				recoverItem();

		if (!(target instanceof LivingEntity)) {
			playHitSound(level, position());
			kill();
			return;
		}

		LivingEntity livingentity = (LivingEntity) target;

		if (type.getReloadTicks() < 10)
			livingentity.invulnerableTime = type.getReloadTicks() + 10;

		if (onServer && knockback > 0) {
			Vec3 appliedMotion = this.getDeltaMovement()
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

		if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer
				&& !this.isSilent()) {
			((ServerPlayer) owner).connection
					.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
		}

		if (onServer && owner instanceof ServerPlayer) {
			ServerPlayer serverplayerentity = (ServerPlayer) owner;
			if (!target.isAlive() && target.getType()
					.getCategory() == MobCategory.MONSTER || (target instanceof Player && target != owner))
				AllAdvancements.POTATO_CANNON.awardTo(serverplayerentity);
		}

		if (type.isSticky() && target.isAlive()) {
			setStuckEntity(target);
		} else {
			kill();
		}

	}

	private void recoverItem() {
		if (!stack.isEmpty())
			spawnAtLocation(ItemHandlerHelper.copyStackWithSize(stack, 1));
	}

	public static void playHitSound(Level world, Vec3 location) {
		AllSoundEvents.POTATO_HIT.playOnServer(world, new BlockPos(location));
	}

	public static void playLaunchSound(Level world, Vec3 location, float pitch) {
		AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
	}

	@Override
	protected void onHitBlock(BlockHitResult ray) {
		PotatoCannonProjectileType projectileType = getProjectileType();
		Vec3 hit = ray.getLocation();
		pop(hit);
		if (!getProjectileType().onBlockHit(level, ray) && !level.isClientSide && projectileType.getSplit() > 0)
			if (random.nextDouble() <= recoveryChance)
				recoverItem();

		split(projectileType);

		super.onHitBlock(ray);
		kill();
	}

	public void split(PotatoCannonProjectileType type) {

		if (type.getSplit() > 0 && splitAmount > 0)
			for (int i = 0; i < type.getSplit(); i++) {

				float random = Create.RANDOM.nextFloat(0, 100);
				float chance = 0;
				PotatoCannonProjectileType chosenType = null;
				for (SplitProperty splitType : type.getSplitInto()){
					chance += splitType.chance();
					if (random < chance) {
						chosenType = splitType.type();
						break;
					}
				}
				if (chosenType == null)
					continue;

				PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(level);

				if (!type.getSplitInto().isEmpty()) {
					projectile.type = chosenType;
					if (projectile.type.getOverride() == null)
						projectile.setItem(new ItemStack(projectile.type.getItems().get(0).get()));
					else
						projectile.setItem(new ItemStack(projectile.type.getOverride().get()));
				} else {
					projectile.type = type;
					projectile.setItem(this.getItem());
				}

				projectile.setOwner(this.getOwner());
				projectile.randomIdentifer = this.randomIdentifer;

				projectile.setEnchantmentEffectsFromCannon(stack, splitAmount - 1);

				Vec3 sprayBase = VecHelper.rotate(new Vec3(0, type.getSplitSpeed(), 0), 360 * Create.RANDOM.nextFloat(), Direction.Axis.Z);
				float sprayChange = 360f / type.getSplit();

				Vec3 splitMotion = new Vec3(0, 0.5, 0);
				float spread = 100 * (Create.RANDOM.nextFloat() - 0.5f);
				Vec3 sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + spread, Direction.Axis.Y);
				splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, splitMotion));

				if (chosenType.getSplitAmount() == -1 || type.getSplitInto().isEmpty())
					projectile.splitAmount = this.splitAmount - 1;
				else
					projectile.splitAmount = chosenType.getSplitAmount();

				projectile.setPos(this.getX(), this.getY(), this.getZ());
				projectile.setDeltaMovement(splitMotion);
				projectile.setOwner(this.getOwner());
				level.addFreshEntity(projectile);
			}
	}

	@Override
	public boolean hurt(DamageSource source, float amt) {
		if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE)
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
				level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y,
						m.z);
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
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
	}

}
