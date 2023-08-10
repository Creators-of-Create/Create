package com.simibubi.create.content.contraptions.actors.seat;

import com.simibubi.create.AllEntityTypes;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class SeatEntity extends Entity implements IEntityAdditionalSpawnData {

	public SeatEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public SeatEntity(Level world, BlockPos pos) {
		this(AllEntityTypes.SEAT.get(), world);
		noPhysics = true;
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SeatEntity> entityBuilder = (EntityType.Builder<SeatEntity>) builder;
		return entityBuilder.sized(0.25f, 0.35f);
	}

	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		AABB bb = getBoundingBox();
		Vec3 diff = new Vec3(x, y, z).subtract(bb.getCenter());
		setBoundingBox(bb.move(diff));
	}

	@Override
	protected void positionRider(Entity pEntity, Entity.MoveFunction pCallback) {
		if (!this.hasPassenger(pEntity))
			return;
		double d0 = this.getY() + this.getPassengersRidingOffset() + pEntity.getMyRidingOffset();
		pCallback.accept(pEntity, this.getX(), d0 + getCustomEntitySeatOffset(pEntity), this.getZ());
	}

	public static double getCustomEntitySeatOffset(Entity entity) {
		if (entity instanceof Slime)
			return 0.25f;
		if (entity instanceof Parrot)
			return 1 / 16f;
		if (entity instanceof Skeleton)
			return 1 / 8f;
		if (entity instanceof Creeper)
			return 1 / 8f;
		if (entity instanceof Cat)
			return 1 / 8f;
		if (entity instanceof Wolf)
			return 1 / 16f;
		return 0;
	}

	@Override
	public void setDeltaMovement(Vec3 p_213317_1_) {}

	@Override
	public void tick() {
		if (level.isClientSide)
			return;
		boolean blockPresent = level.getBlockState(blockPosition())
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
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundTag p_70037_1_) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag p_213281_1_) {}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public static class Render extends EntityRenderer<SeatEntity> {

		public Render(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		public boolean shouldRender(SeatEntity p_225626_1_, Frustum p_225626_2_, double p_225626_3_, double p_225626_5_,
			double p_225626_7_) {
			return false;
		}

		@Override
		public ResourceLocation getTextureLocation(SeatEntity p_110775_1_) {
			return null;
		}
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {}
}
