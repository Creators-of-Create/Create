package com.simibubi.create.content.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NozzleBlockEntity extends SmartBlockEntity {

	private List<Entity> pushingEntities = new ArrayList<>();
	private float range;
	private boolean pushing;
	private BlockPos fanPos;

	public NozzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(5);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (!clientPacket)
			return;
		compound.putFloat("Range", range);
		compound.putBoolean("Pushing", pushing);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (!clientPacket)
			return;
		range = compound.getFloat("Range");
		pushing = compound.getBoolean("Pushing");
	}

	@Override
	public void initialize() {
		fanPos = worldPosition.relative(getBlockState().getValue(NozzleBlock.FACING)
			.getOpposite());
		super.initialize();
	}

	@Override
	public void tick() {
		super.tick();

		float range = calcRange();
		if (this.range != range)
			setRange(range);

		Vec3 center = VecHelper.getCenterOf(worldPosition);
		if (level.isClientSide && range != 0) {
			if (level.random.nextInt(
				Mth.clamp((AllConfigs.server().kinetics.fanPushDistance.get() - (int) range), 1, 10)) == 0) {
				Vec3 start = VecHelper.offsetRandomly(center, level.random, pushing ? 1 : range / 2);
				Vec3 motion = center.subtract(start)
					.normalize()
					.scale(Mth.clamp(range * (pushing ? .025f : 1f), 0, .5f) * (pushing ? -1 : 1));
				level.addParticle(ParticleTypes.POOF, start.x, start.y, start.z, motion.x, motion.y, motion.z);
			}
		}

		for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			Vec3 diff = entity.position()
					.subtract(center);

			if (!(entity instanceof Player) && level.isClientSide)
				continue;

			double distance = diff.length();
			if (distance > range || entity.isShiftKeyDown() || AirCurrent.isPlayerCreativeFlying(entity)) {
				iterator.remove();
				continue;
			}

			if (!pushing && distance < 1.5f)
				continue;

			float factor = (entity instanceof ItemEntity) ? 1 / 128f : 1 / 32f;
			Vec3 pushVec = diff.normalize()
					.scale((range - distance) * (pushing ? 1 : -1));
			entity.setDeltaMovement(entity.getDeltaMovement()
				.add(pushVec.scale(factor)));
			entity.fallDistance = 0;
			entity.hurtMarked = true;
		}

	}

	public void setRange(float range) {
		this.range = range;
		if (range == 0)
			pushingEntities.clear();
		sendData();
	}

	private float calcRange() {
		BlockEntity be = level.getBlockEntity(fanPos);
		if (!(be instanceof IAirCurrentSource))
			return 0;

		IAirCurrentSource source = (IAirCurrentSource) be;
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

		Vec3 center = VecHelper.getCenterOf(worldPosition);
		AABB bb = new AABB(center, center).inflate(range / 2f);

		for (Entity entity : level.getEntitiesOfClass(Entity.class, bb)) {
			Vec3 diff = entity.position()
					.subtract(center);

			double distance = diff.length();
			if (distance > range || entity.isShiftKeyDown() || AirCurrent.isPlayerCreativeFlying(entity))
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

		if (!pushing && pushingEntities.size() > 256 && !level.isClientSide) {
			level.explode(null, center.x, center.y, center.z, 2, BlockInteraction.NONE);
			for (Iterator<Entity> iterator = pushingEntities.iterator(); iterator.hasNext();) {
				Entity entity = iterator.next();
				entity.discard();
				iterator.remove();
			}
		}

	}

	private boolean canSee(Entity entity) {
		ClipContext context = new ClipContext(entity.position(), VecHelper.getCenterOf(worldPosition),
			Block.COLLIDER, Fluid.NONE, entity);
		return worldPosition.equals(level.clip(context)
			.getBlockPos());
	}

}
