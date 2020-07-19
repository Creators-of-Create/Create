package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllEntityTypes;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.HashMap;
import java.util.Map;

public class SeatEntity extends Entity {

	public static final Map<BlockPos, SeatEntity> TAKEN = new HashMap<>();

	public SeatEntity(EntityType<?> p_i48580_1_, World p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public SeatEntity(World world, BlockPos pos) {
		this(AllEntityTypes.SEAT.get(), world);
		this.setPos(pos.getX() + 0.5, pos.getY() + 0.30, pos.getZ() + 0.5);
		noClip = true;
		TAKEN.put(pos, this);

	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SeatEntity> entityBuilder = (EntityType.Builder<SeatEntity>) builder;
		return entityBuilder.size(0, 0);
	}

	@Override
	public void tick() {
		if (world.isRemote)
			return;

		BlockPos blockPos = new BlockPos(getX(), getY(), getZ());
		if (isBeingRidden() && world.getBlockState(blockPos).getBlock() instanceof SeatBlock)
			return;

		TAKEN.remove(blockPos);
		this.remove();
	}

	@Override
	protected boolean canBeRidden(Entity p_184228_1_) {
		return true;
	}

	@Override
	protected void removePassenger(Entity entity) {
		super.removePassenger(entity);
		Vec3d pos = entity.getPositionVec();
		entity.setPosition(pos.x, pos.y + 0.7, pos.z);
	}

	@Override
	protected void registerData() {}

	@Override
	protected void readAdditional(CompoundNBT p_70037_1_) {}

	@Override
	protected void writeAdditional(CompoundNBT p_213281_1_) {}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public static class Render extends EntityRenderer<SeatEntity> {

		public Render(EntityRendererManager p_i46179_1_) {
			super(p_i46179_1_);
		}

		@Override
		public boolean shouldRender(SeatEntity p_225626_1_, ClippingHelperImpl p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
			return false;
		}

		@Override
		public ResourceLocation getEntityTexture(SeatEntity p_110775_1_) {
			return null;
		}
	}
}
