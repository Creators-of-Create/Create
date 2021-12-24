package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;

import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity implements ExtraSpawnDataEntity {

	public SeatEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public SeatEntity(Level world, BlockPos pos) {
		this(AllEntityTypes.SEAT.get(), world);
		noPhysics = true;
	}

	public static FabricEntityTypeBuilder<?> build(FabricEntityTypeBuilder<?> builder) {
//		@SuppressWarnings("unchecked")
//		EntityType.Builder<SeatEntity> entityBuilder = (EntityType.Builder<SeatEntity>) builder;
		return builder.dimensions(EntityDimensions.fixed(0.25f, 0.35f));
	}

	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		AABB bb = getBoundingBox();
		Vec3 diff = new Vec3(x, y, z).subtract(bb.getCenter());
		setBoundingBox(bb.move(diff));
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
		return !(entity instanceof FakeServerPlayer);
	}

	@Override
	protected void removePassenger(Entity entity) {
		super.removePassenger(entity);
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
		return new ClientboundAddEntityPacket(this);
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
