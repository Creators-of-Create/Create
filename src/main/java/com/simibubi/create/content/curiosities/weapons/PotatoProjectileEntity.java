package com.simibubi.create.content.curiosities.weapons;

import javax.annotation.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;

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
	protected int numberOfPiercing = 0;
	protected final ArrayList<Entity> ENTITY_STRUCK_LIST = new ArrayList<>();

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

	public void setEnchantmentEffectsFromCannon(ItemStack cannon) {
		int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, cannon);
		int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, cannon);
		int flame = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, cannon);
		int piercing = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, cannon);
		int recovery = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.POTATO_RECOVERY.get(), cannon);
		int extraShot = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.MULTIPLITATO.get(), cannon);

		if (power > 0)
			additionalDamageMult = 1 + power * .2f;
		if (punch > 0)
			additionalKnockback = punch * .5f;
		if (flame > 0)
			setSecondsOnFire(100);
		if (piercing > 0)
			numberOfPiercing += piercing;
		if (getProjectileType().getPiercing() > 0)
			numberOfPiercing += getProjectileType().getPiercing();
		if (recovery > 6)
			recoveryChance = 1;
		else if (recovery > 0)
			recoveryChance = .125f + recovery * .125f;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		stack = ItemStack.of(nbt.getCompound("Item"));
		additionalDamageMult = nbt.getFloat("AdditionalDamage");
		additionalKnockback = nbt.getFloat("AdditionalKnockback");
		recoveryChance = nbt.getFloat("Recovery");
		numberOfPiercing = nbt.getInt("NumberOfPiercing");
		super.readAdditionalSaveData(nbt);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		nbt.put("Item", stack.serializeNBT());
		nbt.putFloat("AdditionalDamage", additionalDamageMult);
		nbt.putFloat("AdditionalKnockback", additionalKnockback);
		nbt.putFloat("Recovery", recoveryChance);
		nbt.putFloat("NumberOfPiercing", numberOfPiercing);
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
		super.onHitEntity(ray); // Do the superclass method first

		if (getStuckEntity() != null) // If it's stuck on an entity
			return; // Stop here

		Vec3 hit = ray.getLocation(); // Location of the hit
		Entity target = ray.getEntity(); // Entity that got struck
		PotatoCannonProjectileType projectileType = getProjectileType(); // Projectile that hit the entity
		float damage = projectileType.getDamage() * additionalDamageMult; // Damage of the projectile
		float knockback = projectileType.getKnockback() + additionalKnockback; // Knockback of the projectile
		Entity owner = this.getOwner(); // Owner of the projectile

		if (!target.isAlive()) // If the target is not alive
			return; // Stop here
		if (owner instanceof LivingEntity) // If the owner is a living entity
			((LivingEntity) owner).setLastHurtMob(target); // Set the last mob that the owner hurt

		if (target instanceof PotatoProjectileEntity) { // If the struck entity is a potato projectile
			/*  Condition :
			 *  The projectile hasn't been loaded for 10 tick (0.5s) AND the struck projectile hasn't been loaded ofr 10 tick (0.5s)
			 *  OR
			 *  The owner of both projectile is the same
			 */
			if (tickCount < 10 && target.tickCount < 10 || owner == ((PotatoProjectileEntity) target).getOwner())
				return; // Stop here
		}

		boolean preShot = numberOfPiercing > 0 && !ENTITY_STRUCK_LIST.contains(target); // If it's not the last entity it can hit
		boolean lastShot = numberOfPiercing == 0 && !ENTITY_STRUCK_LIST.contains(target); // If it's the last entity it can hit

		if (target instanceof LivingEntity) { // If the target is a living entity
			if (preShot) {
				ENTITY_STRUCK_LIST.add(target); // Add the entity to the list of entity it cannot hit again anymore
				numberOfPiercing--; // Remove one piercing level
			}
			else if (ENTITY_STRUCK_LIST.contains(target)) // If the entity was struck before
				return; // Stop Here
		}

		if (lastShot) // If it's the last hit
			pop(hit); // Pop that projectile

		if (target instanceof WitherBoss && ((WitherBoss) target).isPowered()) // If it's a wither boss that is in it's anti-projectile phase
			return; // Stop Here

		if (projectileType.preEntityHit(ray)) // MOST LIKELY means that it cannot hit itself
			return;

		boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN; // Is the target an enderman?
		int k = target.getRemainingFireTicks(); // The remaining fire tick on the struck entity

		if (this.isOnFire() && !targetIsEnderman) // If the projectile is on fire AND the target is not an enderman
			target.setSecondsOnFire(5); // Set the target on fire for 5 second

		boolean onServer = !level.isClientSide; // Server Side
		if (onServer && !target.hurt(causePotatoDamage(), damage)) { // (Server Side) If the target wasn't hurt by the projectile
			target.setRemainingFireTicks(k); // Set the fire tick to itself
			if (lastShot) // If it's the last shot
				kill(); // Kill the projectile
			return; // Stop here
		}

		if (targetIsEnderman) // If the target is an enderman
			return; // Stop here

		if (!projectileType.onEntityHit(ray) && onServer) // (Server Side) If the projectile didn't hit something
			if (random.nextDouble() <= recoveryChance && lastShot) // If the random chance succeeded AND it's the last entity
				recoverItem(); // Recover the item

		if (!(target instanceof LivingEntity)) { // Kill any projectile if the target is not a living entity, doesn't care about piercing
			playHitSound(level, position()); // Play a hit sound
			kill(); // Kill the projectile
			return; // Stop there
		}

		LivingEntity livingentity = (LivingEntity) target; // Turn the target to a living entity, a non-living entity will never reach this statement so down casting is safe

		if (type.getReloadTicks() < 10) // If the reload time of the projectile is lesser than 10, get the invulnerability of the mob to reload time + 10
										// Don't ask me WHY it works like this lmao
			livingentity.invulnerableTime = type.getReloadTicks() + 10;

		if (onServer && knockback > 0) { // (Server Side) If the knockback is higher than 0
			Vec3 appliedMotion = this.getDeltaMovement() // Get the vector of the projectile
				.multiply(1.0D, 0.0D, 1.0D) // Remove any vertical movement
				.normalize() // Turn the vector so it's 1 length
				.scale(knockback * 0.6); // Scale the vector depending of the knockback
			if (appliedMotion.lengthSqr() > 0.0D) // If the vector length isn't 0
				livingentity.push(appliedMotion.x, 0.1D, appliedMotion.z); // Push the entity, also give a small vertical jump
		}

		if (onServer && owner instanceof LivingEntity) { // (Server Side) If the owner is a living entity
			EnchantmentHelper.doPostHurtEffects(livingentity, owner);
			EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingentity);
		}

		/*
			Conditions
			If the target is not the owner
			AND the target is a player
			AND the owner is a player too
			AND that the projectile isn't silent
		 */
		if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer
			&& !this.isSilent()) {
			((ServerPlayer) owner).connection
				.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
			// Send a ping for the Arrow Hit Player event
		}

		if (onServer && owner instanceof ServerPlayer) { // (Server Side) If the owner is a player
			ServerPlayer serverplayerentity = (ServerPlayer) owner; // Set the owner
			/*
				Conditions
				If the entity is alive
				AND the target is a Monster
				OR
				The target is a Player
				AND the target is not the owner
			 */
			if (!target.isAlive() && target.getType()
					.getCategory() == MobCategory.MONSTER
					|| (target instanceof Player && target != owner))
					AllTriggers.POTATO_KILL.trigger(serverplayerentity); // What is this supposed to be?
		}

		if (type.isSticky() && target.isAlive()) { // If the projectile is sticky and the target is alive
			setStuckEntity(target); // Stick the entity, it won't care about piercing
		}
		else if (lastShot) // If it's the last entity
			kill(); // Kill the projectile
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
		Vec3 hit = ray.getLocation();
		pop(hit);
		if (!getProjectileType().onBlockHit(level, ray) && !level.isClientSide)
			if (random.nextDouble() <= recoveryChance)
				recoverItem();
		super.onHitBlock(ray);
		kill();
	}

	@Override
	public boolean hurt(DamageSource source, float amt) {
		if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE)
			return false;
		if (this.isInvulnerableTo(source))
			return false;
		if (numberOfPiercing == 0)
			pop(position());
			kill();
		return true;
	}

	private void pop(Vec3 hit) {
		if (!stack.isEmpty()) { // If the projectile is not empty
			for (int i = 0; i < 7; i++) { // Do it 7 time
				Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, this.random, .25f);
				level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y, m.z);
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
