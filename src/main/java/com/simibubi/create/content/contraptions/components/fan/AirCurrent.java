package com.simibubi.create.content.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.content.contraptions.processing.fan.transform.EntityTransformHelper;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class AirCurrent {

	public final IAirCurrentSource source;
	public AABB bounds = new AABB(0, 0, 0, 0, 0, 0);
	public List<AirCurrentSegment> segments = new ArrayList<>();
	public Direction direction;
	public boolean pushing;
	public float maxDistance;

	protected List<Pair<TransportedItemStackHandlerBehaviour, AbstractFanProcessingType>> affectedItemHandlers =
		new ArrayList<>();
	protected List<Entity> caughtEntities = new ArrayList<>();

	static boolean isClientPlayerInAirCurrent;

	public AirCurrent(IAirCurrentSource source) {
		this.source = source;
	}

	public void tick() {
		if (direction == null)
			rebuild();
		Level world = source.getAirCurrentWorld();
		Direction facing = direction;
		if (world != null && world.isClientSide) {
			float offset = pushing ? 0.5f : maxDistance + .5f;
			Vec3 pos = VecHelper.getCenterOf(source.getAirCurrentPos())
				.add(Vec3.atLowerCornerOf(facing.getNormal())
					.scale(offset));
			if (world.random.nextFloat() < AllConfigs.CLIENT.fanParticleDensity.get())
				world.addParticle(new AirFlowParticleData(source.getAirCurrentPos()), pos.x, pos.y, pos.z, 0, 0, 0);
		}

		tickAffectedEntities(world, facing);
		tickAffectedHandlers();
	}

	protected void tickAffectedEntities(Level world, Direction facing) {
		for (Iterator<Entity> iterator = caughtEntities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if (!entity.isAlive() || !entity.getBoundingBox()
				.intersects(bounds) || isPlayerCreativeFlying(entity)) {
				iterator.remove();
				continue;
			}

			Vec3 center = VecHelper.getCenterOf(source.getAirCurrentPos());
			Vec3i flow = (pushing ? facing : facing.getOpposite()).getNormal();

			float sneakModifier = entity.isShiftKeyDown() ? 4096f : 512f;
			float speed = Math.abs(source.getSpeed());
			double entityDistance = entity.position()
				.distanceTo(center);
			float acceleration = (float) (speed / sneakModifier / (entityDistance / maxDistance));
			Vec3 previousMotion = entity.getDeltaMovement();
			float maxAcceleration = 5;

			double xIn = Mth.clamp(flow.getX() * acceleration - previousMotion.x, -maxAcceleration, maxAcceleration);
			double yIn = Mth.clamp(flow.getY() * acceleration - previousMotion.y, -maxAcceleration, maxAcceleration);
			double zIn = Mth.clamp(flow.getZ() * acceleration - previousMotion.z, -maxAcceleration, maxAcceleration);

			entity.setDeltaMovement(previousMotion.add(new Vec3(xIn, yIn, zIn).scale(1 / 8f)));
			entity.fallDistance = 0;
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> enableClientPlayerSound(entity, Mth.clamp(speed / 128f * .4f, 0.01f, .4f)));

			if (entity instanceof ServerPlayer)
				((ServerPlayer) entity).connection.aboveGroundTickCount = 0;

			entityDistance -= .5f;
			AbstractFanProcessingType processingType = getSegmentAt((float) entityDistance);

			if (processingType == null || processingType == AbstractFanProcessingType.NONE)
				continue;

			if (entity instanceof ItemEntity itemEntity) {
				if (world.isClientSide) {
					processingType.spawnParticlesForProcessing(world, entity.position());
					continue;
				}
				if (InWorldProcessing.canProcess(itemEntity, processingType))
					if (InWorldProcessing.applyProcessing(itemEntity, processingType)
						&& source instanceof EncasedFanTileEntity fan)
						fan.award(AllAdvancements.FAN_PROCESSING);
				continue;
			}

			if (world.isClientSide()) {
				EntityTransformHelper.clientEffect(processingType, world, entity);
			}
			processingType.affectEntity(entity, world);
			if (!world.isClientSide()) {
				EntityTransformHelper.serverEffect(processingType, world, entity);
			}
		}

	}

	public void rebuild() {
		if (source.getSpeed() == 0) {
			maxDistance = 0;
			segments.clear();
			bounds = new AABB(0, 0, 0, 0, 0, 0);
			return;
		}

		direction = source.getAirflowOriginSide();
		pushing = source.getAirFlowDirection() == direction;
		maxDistance = source.getMaxDistance();

		Level world = source.getAirCurrentWorld();
		BlockPos start = source.getAirCurrentPos();
		float max = this.maxDistance;
		Direction facing = direction;
		Vec3 directionVec = Vec3.atLowerCornerOf(facing.getNormal());
		maxDistance = getFlowLimit(world, start, max, facing);

		// Determine segments with transported fluids/gases
		AirCurrentSegment currentSegment = new AirCurrentSegment();
		segments.clear();
		currentSegment.startOffset = 0;
		AbstractFanProcessingType type = AbstractFanProcessingType.NONE;

		int limit = (int) (maxDistance + .5f);
		int searchStart = pushing ? 0 : limit;
		int searchEnd = pushing ? limit : 0;
		int searchStep = pushing ? 1 : -1;

		for (int i = searchStart; i * searchStep <= searchEnd * searchStep; i += searchStep) {
			BlockPos currentPos = start.relative(direction, i);
			AbstractFanProcessingType newType = AbstractFanProcessingType.byBlock(world, currentPos);
			if (newType != AbstractFanProcessingType.NONE)
				type = newType;
			if (currentSegment.type != type || currentSegment.startOffset == 0) {
				currentSegment.endOffset = i;
				if (currentSegment.startOffset != 0)
					segments.add(currentSegment);
				currentSegment = new AirCurrentSegment();
				currentSegment.startOffset = i;
				currentSegment.type = type;
			}
		}
		currentSegment.endOffset = searchEnd + searchStep;
		segments.add(currentSegment);

		// Build Bounding Box
		if (maxDistance < 0.25f)
			bounds = new AABB(0, 0, 0, 0, 0, 0);
		else {
			float factor = maxDistance - 1;
			Vec3 scale = directionVec.scale(factor);
			if (factor > 0)
				bounds = new AABB(start.relative(direction)).expandTowards(scale);
			else {
				bounds = new AABB(start.relative(direction)).contract(scale.x, scale.y, scale.z)
					.move(scale);
			}
		}
		findAffectedHandlers();
	}

	public static float getFlowLimit(Level world, BlockPos start, float max, Direction facing) {
		Vec3 directionVec = Vec3.atLowerCornerOf(facing.getNormal());
		Vec3 planeVec = VecHelper.axisAlingedPlaneOf(directionVec);

		// 4 Rays test for holes in the shapes blocking the flow
		float offsetDistance = .25f;
		Vec3[] offsets = new Vec3[] { planeVec.multiply(offsetDistance, offsetDistance, offsetDistance),
			planeVec.multiply(-offsetDistance, -offsetDistance, offsetDistance),
			planeVec.multiply(offsetDistance, -offsetDistance, -offsetDistance),
			planeVec.multiply(-offsetDistance, offsetDistance, -offsetDistance), };

		float limitedDistance = 0;

		// Determine the distance of the air flow
		Outer: for (int i = 1; i <= max; i++) {
			BlockPos currentPos = start.relative(facing, i);
			if (!world.isLoaded(currentPos))
				break;
			BlockState state = world.getBlockState(currentPos);
			if (shouldAlwaysPass(state))
				continue;
			VoxelShape voxelshape = state.getCollisionShape(world, currentPos, CollisionContext.empty());
			if (voxelshape.isEmpty())
				continue;
			if (voxelshape == Shapes.block()) {
				max = i - 1;
				break;
			}

			for (Vec3 offset : offsets) {
				Vec3 rayStart = VecHelper.getCenterOf(currentPos)
					.subtract(directionVec.scale(.5f + 1 / 32f))
					.add(offset);
				Vec3 rayEnd = rayStart.add(directionVec.scale(1 + 1 / 32f));
				BlockHitResult blockraytraceresult =
					world.clipWithInteractionOverride(rayStart, rayEnd, currentPos, voxelshape, state);
				if (blockraytraceresult == null)
					continue Outer;

				double distance = i - 1 + blockraytraceresult.getLocation()
					.distanceTo(rayStart);
				if (limitedDistance < distance)
					limitedDistance = (float) distance;
			}

			max = limitedDistance;
			break;
		}
		return max;
	}

	public void findEntities() {
		caughtEntities.clear();
		caughtEntities = source.getAirCurrentWorld()
			.getEntities(null, bounds);
	}

	public void findAffectedHandlers() {
		Level world = source.getAirCurrentWorld();
		BlockPos start = source.getAirCurrentPos();
		affectedItemHandlers.clear();
		for (int i = 0; i < maxDistance + 1; i++) {
			AbstractFanProcessingType type = getSegmentAt(i);
			if (type == null)
				continue;

			for (int offset : Iterate.zeroAndOne) {
				BlockPos pos = start.relative(direction, i)
					.below(offset);
				TransportedItemStackHandlerBehaviour behaviour =
					TileEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE);
				if (behaviour != null)
					affectedItemHandlers.add(Pair.of(behaviour, type));
				if (direction.getAxis()
					.isVertical())
					break;
			}
		}
	}

	public void tickAffectedHandlers() {
		for (Pair<TransportedItemStackHandlerBehaviour, AbstractFanProcessingType> pair : affectedItemHandlers) {
			TransportedItemStackHandlerBehaviour handler = pair.getKey();
			Level world = handler.getWorld();
			AbstractFanProcessingType  processingType = pair.getRight();

			handler.handleProcessingOnAllItems((transported) -> {
				if (world.isClientSide) {
					if (world != null)
						processingType.spawnParticlesForProcessing(world, handler.getWorldPositionOf(transported));
					return TransportedResult.doNothing();
				}
				TransportedResult applyProcessing = InWorldProcessing.applyProcessing(transported, world, processingType);
				if (!applyProcessing.doesNothing() && source instanceof EncasedFanTileEntity fan)
					fan.award(AllAdvancements.FAN_PROCESSING);
				return applyProcessing;
			});
		}
	}

	private static boolean shouldAlwaysPass(BlockState state) {
		return AllTags.AllBlockTags.FAN_TRANSPARENT.matches(state);
	}

	public AbstractFanProcessingType getSegmentAt(float offset) {
		for (AirCurrentSegment airCurrentSegment : segments) {
			if (offset > airCurrentSegment.endOffset && pushing)
				continue;
			if (offset < airCurrentSegment.endOffset && !pushing)
				continue;
			return airCurrentSegment.type;
		}
		return AbstractFanProcessingType.NONE;
	}

	public static class AirCurrentSegment {
		AbstractFanProcessingType type;
		int startOffset;
		int endOffset;
	}

	@OnlyIn(Dist.CLIENT)
	static AirCurrentSound flyingSound;

	@OnlyIn(Dist.CLIENT)
	private static void enableClientPlayerSound(Entity e, float maxVolume) {
		if (e != Minecraft.getInstance()
			.getCameraEntity())
			return;

		isClientPlayerInAirCurrent = true;

		float pitch = (float) Mth.clamp(e.getDeltaMovement()
			.length() * .5f, .5f, 2f);

		if (flyingSound == null || flyingSound.isStopped()) {
			flyingSound = new AirCurrentSound(SoundEvents.ELYTRA_FLYING, pitch);
			Minecraft.getInstance()
				.getSoundManager()
				.play(flyingSound);
		}
		flyingSound.setPitch(pitch);
		flyingSound.fadeIn(maxVolume);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tickClientPlayerSounds() {
		if (!AirCurrent.isClientPlayerInAirCurrent && flyingSound != null)
			if (flyingSound.isFaded())
				flyingSound.stopSound();
			else
				flyingSound.fadeOut();
		isClientPlayerInAirCurrent = false;
	}

	public static boolean isPlayerCreativeFlying(Entity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			return player.isCreative() && player.getAbilities().flying;
		}
		return false;
	}

}
