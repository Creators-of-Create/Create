package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllEntityTypes;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class SeatEntity extends Entity implements IEntityAdditionalSpawnData {

	public SeatEntity(EntityType<?> p_i48580_1_, World p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public SeatEntity(World world, BlockPos pos) {
		this(AllEntityTypes.SEAT.get(), world);
		noClip = true;
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SeatEntity> entityBuilder = (EntityType.Builder<SeatEntity>) builder;
		return entityBuilder.size(0.25f, 0.35f);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return super.getBoundingBox();
	}
	
	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		AxisAlignedBB bb = getBoundingBox();
		Vector3d diff = new Vector3d(x, y, z).subtract(bb.getCenter());
		setBoundingBox(bb.offset(diff));
	}

	@Override
	public void setMotion(Vector3d p_213317_1_) {}

	@Override
	public void tick() {
		if (world.isRemote) 
			return;
		boolean blockPresent = world.getBlockState(getBlockPos())
			.getBlock() instanceof SeatBlock;
		if (isBeingRidden() && blockPresent)
			return;
		this.remove();
	}

	@Override
	protected boolean canBeRidden(Entity entity) {
		// Fake Players (tested with deployers) have a BUNCH of weird issues, don't let them ride seats
		return !(entity instanceof FakePlayer);
	}

	@Override
	protected void removePassenger(Entity entity) {
		super.removePassenger(entity);
		Vector3d pos = entity.getPositionVec();
		entity.setPosition(pos.x, pos.y + 0.85f, pos.z);
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
		public boolean shouldRender(SeatEntity p_225626_1_, ClippingHelper p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
			return false;
		}

		@Override
		public ResourceLocation getEntityTexture(SeatEntity p_110775_1_) {
			return null;
		}
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {}
}
