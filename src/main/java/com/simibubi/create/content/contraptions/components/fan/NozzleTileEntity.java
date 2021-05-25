package com.simibubi.create.content.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion.Mode;

public class NozzleTileEntity extends SmartTileEntity {

	private List<Entity> pushingEntities = new ArrayList<>();
	private float range;
	private boolean pushing;
	private BlockPos fanPos;

	public NozzleTileEntity(TileEntityType<? extends NozzleTileEntity> type) {
		super(type);
		setLazyTickRate(5);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (!clientPacket)
			return;
		compound.putFloat("Range", range);
		compound.putBoolean("Pushing", pushing);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (!clientPacket)
			return;
		range = compound.getFloat("Range");
		pushing = compound.getBoolean("Pushing");
	}

	@Override
	public void initialize() {
		fanPos = pos.offset(getBlockState().get(NozzleBlock.FACING)
			.getOpposite());
		super.initialize();
	}

	@Override
	public void tick() {
		super.tick();

		float range = calcRange();
		if (this.range != range)
			setRange(range);

		Vector3d center = VecHelper.getCenterOf(pos);
		if (world.isRemote && range != 0) {
			if (world.rand.nextInt(
				MathHelper.clamp((AllConfigs.SERVER.kinetics.fanPushDistance.get() - (int) range), 1, 10)) == 0) {
				Vector3d start = VecHelper.offsetRandomly(center, world.rand, pushing ? 1 : range / 2);
				Vector3d motion = center.subtract(start)
					.normalize()
					.scale(MathHelper.clamp(range * (pushing ? .025f : 1f), 0, .5f) * (pushing ? -1 : 1));
				world.addParticle(ParticleTypes.POOF, start.x, start.y, start.z, motion.x, motion.y, motion.z);
			}
		}

		for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			Vector3d diff = entity.getPositionVec()
				.subtract(center);

			if (!(entity instanceof PlayerEntity) && world.isRemote)
				continue;

			double distance = diff.length();
			if (distance > range || entity.isSneaking() || AirCurrent.isPlayerCreativeFlying(entity)) {
				iterator.remove();
				continue;
			}

			if (!pushing && distance < 1.5f)
				continue;

			float factor = (entity instanceof ItemEntity) ? 1 / 128f : 1 / 32f;
			Vector3d pushVec = diff.normalize()
				.scale((range - distance) * (pushing ? 1 : -1));
			entity.setMotion(entity.getMotion()
				.add(pushVec.scale(factor)));
			entity.fallDistance = 0;
			entity.velocityChanged = true;
		}

	}

	public void setRange(float range) {
		this.range = range;
		if (range == 0)
			pushingEntities.clear();
		sendData();
	}

	private float calcRange() {
		TileEntity te = world.getTileEntity(fanPos);
		if (!(te instanceof IAirCurrentSource))
			return 0;

		IAirCurrentSource source = (IAirCurrentSource) te;
		if (source instanceof EncasedFanTileEntity && ((EncasedFanTileEntity) source).isGenerator)
			return 0;
		if (source.getAirCurrent() == null)
			return 0;
		if (source.getSpeed() == 0)
			return 0;
		pushing = source.getAirFlowDirection() == source.getAirflowOriginSide();
		return source.getMaxDistance();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (range == 0)
			return;

		Vector3d center = VecHelper.getCenterOf(pos);
		AxisAlignedBB bb = new AxisAlignedBB(center, center).grow(range / 2f);

		for (Entity entity : world.getEntitiesWithinAABB(Entity.class, bb)) {
			Vector3d diff = entity.getPositionVec()
				.subtract(center);

			double distance = diff.length();
			if (distance > range || entity.isSneaking() || AirCurrent.isPlayerCreativeFlying(entity))
				continue;

			boolean canSee = canSee(entity);
			if (!canSee) {
				pushingEntities.remove(entity);
				continue;
			}

			if (!pushingEntities.contains(entity))
				pushingEntities.add(entity);
		}

		for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if (entity.isAlive())
				continue;
			iterator.remove();
		}

		if (!pushing && pushingEntities.size() > 256 && !world.isRemote) {
			world.createExplosion(null, center.x, center.y, center.z, 2, Mode.NONE);
			for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
				Entity entity = iterator.next();
				entity.remove();
				iterator.remove();
			}
		}

	}

	private boolean canSee(Entity entity) {
		RayTraceContext context = new RayTraceContext(entity.getPositionVec(), VecHelper.getCenterOf(pos),
			BlockMode.COLLIDER, FluidMode.NONE, entity);
		return pos.equals(world.rayTraceBlocks(context)
			.getPos());
	}

}
