package com.simibubi.create.modules.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion.Mode;

public class NozzleTileEntity extends SmartTileEntity {

	private List<Entity> pushingEntities = new ArrayList<>();
	private float range;
	private boolean pushing;
	private BlockPos fanPos;

	public NozzleTileEntity() {
		super(AllTileEntities.NOZZLE.type);
		setLazyTickRate(5);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		compound.putFloat("Range", range);
		compound.putBoolean("Pushing", pushing);
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		range = tag.getFloat("Range");
		pushing = tag.getBoolean("Pushing");
		super.readClientUpdate(tag);
	}

	@Override
	public void initialize() {
		super.initialize();
		fanPos = pos.offset(getBlockState().get(NozzleBlock.FACING).getOpposite());
		lazyTick();
	}

	@Override
	public void tick() {
		super.tick();

		float range = calcRange();
		if (this.range != range)
			setRange(range);

		Vec3d center = VecHelper.getCenterOf(pos);
		if (world.isRemote && range != 0) {
			if (world.rand.nextInt(
					MathHelper.clamp((AllConfigs.SERVER.kinetics.fanPushDistance.get() - (int) range), 1, 10)) == 0) {
				Vec3d start = VecHelper.offsetRandomly(center, world.rand, pushing ? 1 : range / 2);
				Vec3d motion = center.subtract(start).normalize()
						.scale(MathHelper.clamp(range * (pushing ? .025f : 1f), 0, .5f) * (pushing ? -1 : 1));
				world.addParticle(ParticleTypes.POOF, start.x, start.y, start.z, motion.x, motion.y, motion.z);
			}
		}

		for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			Vec3d diff = entity.getPositionVec().subtract(center);

			if (!(entity instanceof PlayerEntity) && world.isRemote)
				continue;

			double distance = diff.length();
			if (distance > range || entity.isSneaking()
					|| (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())) {
				iterator.remove();
				continue;
			}

			if (!pushing && distance < 1.5f)
				continue;

			float factor = (entity instanceof ItemEntity) ? 1 / 128f : 1 / 32f;
			Vec3d pushVec = diff.normalize().scale((range - distance) * (pushing ? 1 : -1));
			entity.setMotion(entity.getMotion().add(pushVec.scale(factor)));
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
		if (!(te instanceof EncasedFanTileEntity))
			return 0;

		EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
		if (fan.isGenerator)
			return 0;
		if (fan.airCurrent == null)
			return 0;
		if (fan.getSpeed() == 0)
			return 0;
		pushing = fan.getSpeed() > 0;
		return fan.getMaxDistance();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (range == 0)
			return;

		Vec3d center = VecHelper.getCenterOf(pos);
		AxisAlignedBB bb = new AxisAlignedBB(center, center).grow(range / 2f);

		for (Entity entity : world.getEntitiesWithinAABB(Entity.class, bb)) {
			Vec3d diff = entity.getPositionVec().subtract(center);

			double distance = diff.length();
			if (distance > range || entity.isSneaking()
					|| (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())) {
				continue;
			}

			boolean canSee = canSee(entity);
			if (!canSee) {
				pushingEntities.remove(entity);
				continue;
			}

			if (!pushingEntities.contains(entity))
				pushingEntities.add(entity);
		}

		if (!pushing && pushingEntities.size() > 512 && !world.isRemote)
			world.createExplosion(null, center.x, center.y, center.z, 6, Mode.BREAK);

	}

	private boolean canSee(Entity entity) {
		RayTraceContext context = new RayTraceContext(entity.getPositionVec(), VecHelper.getCenterOf(pos),
				BlockMode.COLLIDER, FluidMode.NONE, entity);
		return pos.equals(world.rayTraceBlocks(context).getPos());
	}

}
