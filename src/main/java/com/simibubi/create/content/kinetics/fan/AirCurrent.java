package com.simibubi.create.content.kinetics.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

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

	protected List<Pair<TransportedItemStackHandlerBehaviour, FanProcessingType>> affectedItemHandlers =
		new ArrayList<>();
	protected List<Entity> caughtEntities = new ArrayList<>();

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
			if (world.random.nextFloat() < AllConfigs.client().fanParticleDensity.get())
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
			FanProcessingType processingType = getTypeAt((float) entityDistance);

			if (processingType == AllFanProcessingTypes.NONE)
				continue;

			if (entity instanceof ItemEntity itemEntity) {
				if (world != null && world.isClientSide) {
					processingType.spawnProcessingParticles(world, entity.position());
					continue;
				}
				if (FanProcessing.canProcess(itemEntity, processingType))
					if (FanProcessing.applyProcessing(itemEntity, processingType)
						&& source instanceof EncasedFanBlockEntity fan)
						fan.award(AllAdvancements.FAN_PROCESSING);
				continue;
			}

			if (world != null)
				processingType.affectEntity(entity, world);
		}
	}

	public static boolean isPlayerCreativeFlying(Entity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			return player.isCreative() && player.getAbilities().flying;
		}
		return false;
	}

	public void tickAffectedHandlers() {
		for (Pair<TransportedItemStackHandlerBehaviour, FanProcessingType> pair : affectedItemHandlers) {
			TransportedItemStackHandlerBehaviour handler = pair.getKey();
			Level world = handler.getWorld();
			FanProcessingType processingType = pair.getRight();

			handler.handleProcessingOnAllItems(transported -> {
				if (world.isClientSide) {
					processingType.spawnProcessingParticles(world, handler.getWorldPositionOf(transported));
					return TransportedResult.doNothing();
				}
				TransportedResult applyProcessing = FanProcessing.applyProcessing(transported, world, processingType);
				if (!applyProcessing.doesNothing() && source instanceof EncasedFanBlockEntity fan)
					fan.award(AllAdvancements.FAN_PROCESSING);
				return applyProcessing;
			});
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
		segments.clear();
		AirCurrentSegment currentSegment = null;
		FanProcessingType type = AllFanProcessingTypes.NONE;

		int limit = getLimit();
		int searchStart = pushing ? 1 : limit;
		int searchEnd = pushing ? limit : 1;
		int searchStep = pushing ? 1 : -1;
		int toOffset = pushing ? -1 : 0;

		for (int i = searchStart; i * searchStep <= searchEnd * searchStep; i += searchStep) {
			BlockPos currentPos = start.relative(direction, i);
			FanProcessingType newType = FanProcessingType.getAt(world, currentPos);
			if (newType != AllFanProcessingTypes.NONE) {
				type = newType;
			}
			if (currentSegment == null) {
				currentSegment = new AirCurrentSegment();
				currentSegment.startOffset = i + toOffset;
				currentSegment.type = type;
			} else if (currentSegment.type != type) {
				currentSegment.endOffset = i + toOffset;
				segments.add(currentSegment);
				currentSegment = new AirCurrentSegment();
				currentSegment.startOffset = i + toOffset;
				currentSegment.type = type;
			}
		}
		if (currentSegment != null) {
			currentSegment.endOffset = searchEnd + searchStep + toOffset;
			segments.add(currentSegment);
		}

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
			BlockState copycatState = CopycatBlock.getMaterial(world, currentPos);
			if (shouldAlwaysPass(copycatState.isAir() ? state : copycatState))
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

	private static boolean shouldAlwaysPass(BlockState state) {
		return AllTags.AllBlockTags.FAN_TRANSPARENT.matches(state);
	}

	private int getLimit() {
		if ((float) (int) maxDistance == maxDistance) {
			return (int) maxDistance;
		} else {
			return (int) maxDistance + 1;
		}
	}

	public void findAffectedHandlers() {
		Level world = source.getAirCurrentWorld();
		BlockPos start = source.getAirCurrentPos();
		affectedItemHandlers.clear();
		int limit = getLimit();
		for (int i = 1; i <= limit; i++) {
			FanProcessingType segmentType = getTypeAt(i - 1);
			for (int offset : Iterate.zeroAndOne) {
				BlockPos pos = start.relative(direction, i)
					.below(offset);
				TransportedItemStackHandlerBehaviour behaviour =
					BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE);
				if (behaviour != null) {
					FanProcessingType type = FanProcessingType.getAt(world, pos);
					if (type == AllFanProcessingTypes.NONE)
						type = segmentType;
					affectedItemHandlers.add(Pair.of(behaviour, type));
				}
				if (direction.getAxis()
					.isVertical())
					break;
			}
		}
	}

	public void findEntities() {
		caughtEntities.clear();
		caughtEntities = source.getAirCurrentWorld()
			.getEntities(null, bounds);
	}

	public FanProcessingType getTypeAt(float offset) {
		if (offset >= 0 && offset <= maxDistance) {
			if (pushing) {
				for (AirCurrentSegment airCurrentSegment : segments) {
					if (offset <= airCurrentSegment.endOffset) {
						return airCurrentSegment.type;
					}
				}
			} else {
				for (AirCurrentSegment airCurrentSegment : segments) {
					if (offset >= airCurrentSegment.endOffset) {
						return airCurrentSegment.type;
					}
				}
			}
		}
		return AllFanProcessingTypes.NONE;
	}

	private static class AirCurrentSegment {
		private FanProcessingType type;
		private int startOffset;
		private int endOffset;
	}

	private static boolean isClientPlayerInAirCurrent;

	@OnlyIn(Dist.CLIENT)
	private static AirCurrentSound flyingSound;

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
		if (!isClientPlayerInAirCurrent && flyingSound != null)
			if (flyingSound.isFaded())
				flyingSound.stopSound();
			else
				flyingSound.fadeOut();
		isClientPlayerInAirCurrent = false;
	}

}
