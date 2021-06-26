package com.simibubi.create.content.curiosities.weapons;

import javax.annotation.Nullable;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

public class PotatoProjectileEntity extends DamagingProjectileEntity implements IEntityAdditionalSpawnData {

	ItemStack stack = ItemStack.EMPTY;
	PotatoCannonProjectileTypes type;

	public PotatoProjectileEntity(EntityType<? extends DamagingProjectileEntity> type, World world) {
		super(type, world);
	}

	public ItemStack getItem() {
		return stack;
	}

	public void setItem(ItemStack stack) {
		this.stack = stack;
	}

	public PotatoCannonProjectileTypes getProjectileType() {
		if (type == null)
			type = PotatoCannonProjectileTypes.getProjectileTypeOf(stack)
				.orElse(PotatoCannonProjectileTypes.FALLBACK);
		return type;
	}

	@Override
	public void readAdditional(CompoundNBT nbt) {
		stack = ItemStack.read(nbt.getCompound("Item"));
		super.readAdditional(nbt);
	}

	@Override
	public void writeAdditional(CompoundNBT nbt) {
		nbt.put("Item", stack.serializeNBT());
		super.writeAdditional(nbt);
	}

	public void tick() {
		PotatoCannonProjectileTypes projectileType = getProjectileType();
		setMotion(getMotion().add(0, -.05 * projectileType.getGravityMultiplier(), 0)
			.scale(projectileType.getDrag()));
		super.tick();
	}

	@Override
	protected float getMotionFactor() {
		return 1;
	}

	@Override
	protected IParticleData getParticle() {
		return new AirParticleData(1, 10);
	}

	@Override
	protected boolean isFireballFiery() {
		return false;
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult ray) {
		super.onEntityHit(ray);

		Vector3d hit = ray.getHitVec();
		Entity target = ray.getEntity();
		PotatoCannonProjectileTypes projectileType = getProjectileType();
		int damage = projectileType.getDamage();
		float knockback = projectileType.getKnockback();
		Entity owner = this.getOwner();

		if (!target.isAlive())
			return;
		if (owner instanceof LivingEntity)
			((LivingEntity) owner).setLastAttackedEntity(target);
		if (target instanceof PotatoProjectileEntity && ticksExisted < 10 && target.ticksExisted < 10)
			return;

		pop(hit);

		boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;
		int k = target.getFireTimer();
		if (this.isBurning() && !targetIsEnderman)
			target.setFire(5);

		if (!target.attackEntityFrom(causePotatoDamage(), (float) damage)) {
			target.setFireTicks(k);
			remove();
			return;
		}

		if (targetIsEnderman)
			return;

		projectileType.onEntityHit(ray);

		if (!(target instanceof LivingEntity)) {
			playHitSound(world, getPositionVec());
			remove();
			return;
		}

		LivingEntity livingentity = (LivingEntity) target;
		if (knockback > 0) {
			Vector3d appliedMotion = this.getMotion()
				.mul(1.0D, 0.0D, 1.0D)
				.normalize()
				.scale(knockback * 0.6);
			if (appliedMotion.lengthSquared() > 0.0D)
				livingentity.addVelocity(appliedMotion.x, 0.1D, appliedMotion.z);
		}

		boolean onServer = !world.isRemote;
		if (onServer && owner instanceof LivingEntity) {
			EnchantmentHelper.applyThornEnchantments(livingentity, owner);
			EnchantmentHelper.applyArthropodEnchantments((LivingEntity) owner, livingentity);
		}

		if (owner != null && livingentity != owner && livingentity instanceof PlayerEntity
			&& owner instanceof ServerPlayerEntity && !this.isSilent()) {
			((ServerPlayerEntity) owner).connection
				.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.PROJECTILE_HIT_PLAYER, 0.0F));
		}

		if (onServer && owner instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) owner;
			if (!target.isAlive())
				AllTriggers.POTATO_KILL.trigger(serverplayerentity);
		}

		remove();
	}

	public static void playHitSound(World world, Vector3d location) {
		AllSoundEvents.POTATO_HIT.playOnServer(world, new BlockPos(location));
	}

	public static void playLaunchSound(World world, BlockPos location, float pitch) {
		AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
	}

	@Override
	protected void onBlockHit(BlockRayTraceResult ray) {
		Vector3d hit = ray.getHitVec();
		pop(hit);
		getProjectileType().onBlockHit(world, ray);
		super.onBlockHit(ray);
		remove();
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_) {
		if (this.isInvulnerableTo(p_70097_1_))
			return false;
		pop(getPositionVec());
		remove();
		return true;
	}

	private void pop(Vector3d hit) {
		if (!stack.isEmpty()) {
			for (int i = 0; i < 7; i++) {
				Vector3d m = VecHelper.offsetRandomly(Vector3d.ZERO, this.rand, .25f);
				world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y, m.z);
			}
		}
		if (!world.isRemote)
			playHitSound(world, getPositionVec());
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
		return entityBuilder.size(.25f, .25f);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		writeAdditional(compound);
		buffer.writeCompoundTag(compound);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditional(additionalData.readCompoundTag());
	}

}
