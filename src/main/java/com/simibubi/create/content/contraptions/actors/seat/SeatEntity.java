package com.simibubi.create.content.contraptions.actors.seat;

import com.simibubi.create.AllEntityTypes;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class SeatEntity extends Entity implements IEntityWithComplexSpawn {
	public SeatEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public SeatEntity(Level level) {
		this(AllEntityTypes.SEAT.get(), level);
		noPhysics = true;
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SeatEntity> entityBuilder = (EntityType.Builder<SeatEntity>) builder;
		// This should probably be set to (0, 0) in the next version that allows breaking changes, as seats with boxes to be detected can potentially affect the AABB search (know this because I tried).
		return entityBuilder.sized(0.25f, 0.25f);
	}

	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		AABB bb = getBoundingBox();
		Vec3 diff = new Vec3(x, y, z).subtract(bb.getCenter());
		setBoundingBox(bb.move(diff));
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
		return Vec3.ZERO;
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		if (!this.hasPassenger(passenger))
			return;

		// Ignoring getPassengerAttachmentPoint here, as it is and SHOULD be 0 (both contraptions and seat blocks).
		// The 1.0E-4D is to prevent mobs from Z-fighting with the seat model (though only Slimes with the smallest size are found to do this currently).
		callback.accept(passenger, this.getX(),
			this.getY() + 0.5D - passenger.getVehicleAttachmentPoint(this).y + 1.0E-4D, this.getZ());
	}

	@Override
	public void onPassengerTurned(Entity entity) {
		entity.setYHeadRot(entity.getYRot());
	}

	@Override
	public void setDeltaMovement(Vec3 vec) {
	}

	@Override
	public void tick() {
		if (level().isClientSide)
			return;
		boolean blockPresent = level().getBlockState(blockPosition())
			.getBlock() instanceof SeatBlock;
		if (isVehicle() && blockPresent)
			return;
		this.discard();
	}

	@Override
	protected boolean canRide(Entity entity) {
		// Fake Players (tested with deployers) have a BUNCH of weird issues, don't let
		// them ride seats
		return !(entity instanceof FakePlayer);
	}

	@Override
	protected void removePassenger(Entity entity) {
		super.removePassenger(entity);
		if (entity instanceof TamableAnimal ta)
			ta.setInSittingPose(false);
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
		return super.getDismountLocationForPassenger(pLivingEntity).add(0, 0.5f, 0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	public static class Render extends EntityRenderer<SeatEntity> {

		public Render(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		public boolean shouldRender(SeatEntity seatEntity, Frustum frustum, double p_225626_3_, double p_225626_5_,
			double p_225626_7_) {
			return false;
		}

		@Override
		public ResourceLocation getTextureLocation(SeatEntity seatEntity) {
			return null;
		}
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {}
}
