package com.simibubi.create.modules.contraptions.receivers.constructs.mounted;

import com.simibubi.create.AllEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import net.minecraftforge.fml.network.NetworkHooks;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	protected MountedContraption contraption;
	protected float initialAngle;
	
	enum MovementType {
		TRANSLATION, ROTATION, MOUNTED;
	}

	// Not synchronizing any of these
	public float targetYaw;
	public float targetPitch;
	public float contraptionYaw;
	public float contraptionPitch;

	public ContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
	}

	protected ContraptionEntity(World world) {
		this(AllEntities.CONTRAPTION.type, world);
	}

	public ContraptionEntity(World world, MountedContraption contraption, float initialAngle) {
		this(world);
		this.contraption = contraption;
		this.initialAngle = initialAngle;
		this.prevRotationYaw = initialAngle;
		this.contraptionYaw = initialAngle;
		this.targetYaw = initialAngle;
	}

	@Override
	protected void registerData() {
	}

	@Override
	public void tick() {
		super.tick();
		Entity e = getRidingEntity();
		if (e == null)
			remove();
		else {
			Vec3d movementVector = e.getMotion();
			Vec3d motion = movementVector.normalize();
			if (motion.length() > 0) {
				targetYaw = yawFromMotion(motion);
				targetPitch = (float) ((Math.atan(motion.y) * 73.0D) / Math.PI * 180);
				if (targetYaw < 0)
					targetYaw += 360;
				if (contraptionYaw < 0)
					contraptionYaw += 360;
			}

			float speed = 0.2f;
			prevRotationYaw = contraptionYaw;
			contraptionYaw = angleLerp(speed, contraptionYaw, targetYaw);
			prevRotationPitch = contraptionPitch;
			contraptionPitch = angleLerp(speed, contraptionPitch, targetPitch);

			tickActors(movementVector);
		}
	}

	public void tickActors(Vec3d movementVector) {
		contraption.getActors().forEach(pair -> {
			MovementContext context = pair.right;
			float deg = -contraptionYaw + initialAngle;
			context.motion = VecHelper.rotate(movementVector, deg, Axis.Y);

			if (context.world == null)
				context.world = world;

			Vec3d offset = new Vec3d(pair.left.pos.subtract(contraption.getAnchor()));
			world.addParticle(ParticleTypes.BUBBLE, offset.x, offset.y, offset.z, 0, 0, 0);
			
			offset = VecHelper.rotate(offset, deg, Axis.Y);
			world.addParticle(ParticleTypes.CRIT, offset.x, offset.y, offset.z, 0, 0, 0);
			
			offset = offset.add(new Vec3d(getPosition()).add(0.5, 0, 0.5));
			world.addParticle(ParticleTypes.NOTE, offset.x, offset.y, offset.z, 0, 10, 0);
			
			if (world.isRemote)
				return;

			BlockPos gridPos = new BlockPos(offset);
			if (context.currentGridPos.equals(gridPos))
				return;
			context.currentGridPos = gridPos;

			IHaveMovementBehavior actor = (IHaveMovementBehavior) pair.left.state.getBlock();
			actor.visitPosition(context);
		});
	}

	public static float yawFromMotion(Vec3d motion) {
		return (float) ((Math.PI / 2 - Math.atan2(motion.z, motion.x)) / Math.PI * 180);
	}

	public float getYaw(float partialTicks) {
		float yaw = contraptionYaw;
		return (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, this.prevRotationYaw, yaw)) - initialAngle;
	}

	public float getPitch(float partialTicks) {
		float pitch = contraptionPitch;
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, this.prevRotationPitch, pitch);
	}

	private float angleLerp(float pct, float current, float target) {
		current = current % 360;
		target = target % 360;
		float shortest_angle = ((((target - current) % 360) + 540) % 360) - 180;
		return current + shortest_angle * pct;
	}

	public boolean hitByEntity(Entity entityIn) {
		return entityIn instanceof PlayerEntity
				? this.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) entityIn), 0.0F)
				: false;
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			if (this.isAlive() && !this.world.isRemote) {
				this.remove();
				this.markVelocityChanged();
			}

			return true;
		}
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<ContraptionEntity> entityBuilder = (EntityType.Builder<ContraptionEntity>) builder;
		return entityBuilder.setCustomClientFactory(ContraptionEntity::spawn).size(1, 1);
	}

	public static ContraptionEntity spawn(SpawnEntity spawnEntity, World world) {
		return new ContraptionEntity(world);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		contraption = new MountedContraption();
		contraption.readNBT(compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		prevRotationYaw = initialAngle;
		contraptionYaw = initialAngle;
		targetYaw = initialAngle;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("Contraption", contraption.writeNBT());
		compound.putFloat("InitialAngle", initialAngle);
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
