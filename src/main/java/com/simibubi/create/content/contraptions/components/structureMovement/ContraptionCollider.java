package com.simibubi.create.content.contraptions.components.structureMovement;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.minecraft.entity.Entity.collideBoundingBoxHeuristically;
import static net.minecraft.entity.Entity.horizontalMag;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.actors.BlockBreakingMovementBehaviour;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.collision.OrientedBB;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ContraptionCollider {

	public static DamageSource damageSourceContraptionSuffocate =
		new DamageSource("create.contraption_suffocate").setDamageBypassesArmor();
	public static boolean wasClientPlayerGrounded;
	public static Cache<World, List<WeakReference<ContraptionEntity>>> activeContraptions = CacheBuilder.newBuilder()
		.expireAfterAccess(40, SECONDS)
		.build();

	@SubscribeEvent
	public static void addSpawnedContraptionsToCollisionList(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof ContraptionEntity))
			return;
		try {
			List<WeakReference<ContraptionEntity>> list = activeContraptions.get(event.getWorld(), ArrayList::new);
			ContraptionEntity contraption = (ContraptionEntity) entity;
			list.add(new WeakReference<>(contraption));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void playerCollisionHappensOnClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		ClientWorld world = Minecraft.getInstance().world;
		if (world == null)
			return;
		runCollisions(world);
	}

	@SubscribeEvent
	public static void entityCollisionHappensPreWorldTick(WorldTickEvent event) {
		if (event.phase == Phase.END)
			return;
		World world = event.world;
		runCollisions(world);
	}

	private static void runCollisions(World world) {
		List<WeakReference<ContraptionEntity>> list = activeContraptions.getIfPresent(world);
		if (list == null)
			return;
		for (Iterator<WeakReference<ContraptionEntity>> iterator = list.iterator(); iterator.hasNext();) {
			WeakReference<ContraptionEntity> weakReference = iterator.next();
			ContraptionEntity contraptionEntity = weakReference.get();
			if (contraptionEntity == null || !contraptionEntity.isAlive()) {
				iterator.remove();
				continue;
			}
			collideEntities(contraptionEntity);
		}
	}

	public static void collideEntities(ContraptionEntity contraptionEntity) {
		World world = contraptionEntity.getEntityWorld();
		Contraption contraption = contraptionEntity.getContraption();
		AxisAlignedBB bounds = contraptionEntity.getBoundingBox();
		Vec3d contraptionPosition = contraptionEntity.getPositionVec();
		Vec3d contraptionRotation = contraptionEntity.getRotationVec();
		contraptionEntity.collidingEntities.clear();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		double conRotX = contraptionRotation.z;
		double conRotY = contraptionRotation.y;
		double conRotZ = contraptionRotation.x;

		for (Entity entity : world.getEntitiesWithinAABB((EntityType<?>) null, bounds.grow(2)
			.expand(0, 32, 0), contraptionEntity::canCollideWith)) {
			boolean serverPlayer = entity instanceof PlayerEntity && !world.isRemote;

			// Transform entity position and motion to local space
			Vec3d centerOfBlock = VecHelper.getCenterOf(BlockPos.ZERO);
			Vec3d entityPosition = entity.getPositionVec();
			AxisAlignedBB entityBounds = entity.getBoundingBox();
			Vec3d centerY = new Vec3d(0, entityBounds.getYSize() / 2, 0);
			Vec3d motion = entity.getMotion();
			boolean axisAlignedCollision = contraptionRotation.equals(Vec3d.ZERO);

			Vec3d position =
				entityPosition.subtract(contraptionEntity.stationary ? centerOfBlock : Vec3d.ZERO.add(0, 0.5, 0))
					.add(centerY);

			position = position.subtract(contraptionPosition);
			position = VecHelper.rotate(position, -conRotX, -conRotY, -conRotZ);
			position = position.add(centerOfBlock)
				.subtract(centerY)
				.subtract(entityPosition);

			// Find all potential block shapes to collide with
			AxisAlignedBB localBB = entityBounds.offset(position)
				.grow(1.0E-7D);
			ReuseableStream<VoxelShape> potentialHits =
				getPotentiallyCollidedShapes(world, contraption, localBB.expand(motion));
			if (potentialHits.createStream()
				.count() == 0)
				continue;

			if (!axisAlignedCollision)
				motion = VecHelper.rotate(motion, -conRotX, -conRotY, -conRotZ);

			// Prepare entity bounds
			OrientedBB obb = new OrientedBB(localBB);
			if (!axisAlignedCollision) {
				Matrix3d rotation = new Matrix3d().asIdentity();
				rotation.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-conRotX)));
				rotation.multiply(new Matrix3d().asYRotation(AngleHelper.rad(conRotY)));
				rotation.multiply(new Matrix3d().asZRotation(AngleHelper.rad(-conRotZ)));
				obb.setRotation(rotation);
			}

//			Vec3d visualizerOrigin = new Vec3d(10, 64, 0);
//			CollisionDebugger.OBB = obb.copy();
//			CollisionDebugger.OBB.move(visualizerOrigin);

			MutableObject<Vec3d> collisionResponse = new MutableObject<>(Vec3d.ZERO);
			MutableObject<Vec3d> allowedMotion = new MutableObject<>(motion);
			MutableBoolean futureCollision = new MutableBoolean(false);
			MutableBoolean surfaceCollision = new MutableBoolean(false);
			Vec3d obbCenter = obb.getCenter();

			// Apply separation maths
			List<AxisAlignedBB> bbs = new ArrayList<>();
			potentialHits.createStream()
				.forEach(shape -> shape.toBoundingBoxList()
					.forEach(bbs::add));

			for (AxisAlignedBB bb : bbs) {
				Vec3d currentResponse = collisionResponse.getValue();
				obb.setCenter(obbCenter.add(currentResponse));
				ContinuousSeparationManifold intersect = obb.intersect(bb, allowedMotion.getValue());
//				OutlineParams params = CreateClient.outliner.showAABB(bb, bb.offset(visualizerOrigin))
//					.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
//				params.colored(0xffffff);

				if (intersect == null)
					continue;
				if (surfaceCollision.isFalse())
					surfaceCollision.setValue(intersect.isSurfaceCollision());

				double timeOfImpact = intersect.getTimeOfImpact();
				if (timeOfImpact > 0 && timeOfImpact < 1) {
					futureCollision.setTrue();
//					Vec3d prev = allowedMotion.getValue();
					allowedMotion.setValue(intersect.getAllowedMotion(allowedMotion.getValue()));
//					Debug.debugChat("Allowed Motion FROM " + prev.toString());
//					Debug.debugChat("Allowed Motion TO " + allowedMotion.getValue()
//						.toString());
//					params.colored(0x4499ff);
					continue;
				}
				Vec3d separation = intersect.asSeparationVec();
				if (separation != null && !separation.equals(Vec3d.ZERO)) {
					collisionResponse.setValue(currentResponse.add(separation));
//					Debug.debugChat("Collision " + currentResponse.add(separation)
//						.toString());
//					params.colored(0xff9944);
				}
			}

//			Debug.debugChat("----");

			// Resolve collision
			Vec3d entityMotion = entity.getMotion();
			Vec3d totalResponse = collisionResponse.getValue();
			Vec3d motionResponse = allowedMotion.getValue();

			if (futureCollision.isTrue() && !serverPlayer) {
				if (!axisAlignedCollision)
					motionResponse = VecHelper.rotate(motionResponse, conRotX, conRotY, conRotZ);
				if (motionResponse.y != entityMotion.y) {
					entity.setMotion(entityMotion.mul(1, 0, 1)
						.add(0, motionResponse.y, 0));
					entityMotion = entity.getMotion();
				}
			}

			if (!axisAlignedCollision)
				totalResponse = VecHelper.rotate(totalResponse, conRotX, conRotY, conRotZ);

			if (surfaceCollision.isTrue()) {
//				entity.handleFallDamage(entity.fallDistance, 1); tunnelling issue
				entity.fallDistance = 0;
				entity.onGround = true;
				if (!serverPlayer) {
					DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> checkForClientPlayerCollision(entity));
				}
			}

			if (totalResponse.equals(Vec3d.ZERO))
				continue;

			double motionX = entityMotion.getX();
			double motionY = entityMotion.getY();
			double motionZ = entityMotion.getZ();
			double intersectX = totalResponse.getX();
			double intersectY = totalResponse.getY();
			double intersectZ = totalResponse.getZ();

			double horizonalEpsilon = 1 / 128f;
			if (motionX != 0 && Math.abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
				entityMotion = entityMotion.mul(0, 1, 1);
			if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
				entityMotion = entityMotion.mul(1, 0, 1);
			if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
				entityMotion = entityMotion.mul(1, 1, 0);

			if (entity instanceof ServerPlayerEntity)
				((ServerPlayerEntity) entity).connection.floatingTickCount = 0;

			if (!serverPlayer) {
				Vec3d allowedMovement = getAllowedMovement(totalResponse, entity);
				contraptionEntity.collidingEntities.add(entity);
				entity.velocityChanged = true;
				entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y,
					entityPosition.z + allowedMovement.z);
				entity.setMotion(entityMotion);
			}
		}

	}

	/** From Entity#getAllowedMovement **/
	static Vec3d getAllowedMovement(Vec3d movement, Entity e) {
		AxisAlignedBB bb = e.getBoundingBox();
		ISelectionContext ctx = ISelectionContext.forEntity(e);
		World world = e.world;
		VoxelShape voxelshape = world.getWorldBorder()
			.getShape();
		Stream<VoxelShape> stream =
			VoxelShapes.compare(voxelshape, VoxelShapes.create(bb.shrink(1.0E-7D)), IBooleanFunction.AND)
				? Stream.empty()
				: Stream.of(voxelshape);
		Stream<VoxelShape> stream1 = world.getEmptyCollisionShapes(e, bb.expand(movement), ImmutableSet.of());
		ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
		Vec3d vec3d = movement.lengthSquared() == 0.0D ? movement
			: collideBoundingBoxHeuristically(e, movement, bb, world, ctx, reuseablestream);
		boolean flag = movement.x != vec3d.x;
		boolean flag1 = movement.y != vec3d.y;
		boolean flag2 = movement.z != vec3d.z;
		boolean flag3 = e.onGround || flag1 && movement.y < 0.0D;
		if (e.stepHeight > 0.0F && flag3 && (flag || flag2)) {
			Vec3d vec3d1 = collideBoundingBoxHeuristically(e, new Vec3d(movement.x, (double) e.stepHeight, movement.z),
				bb, world, ctx, reuseablestream);
			Vec3d vec3d2 = collideBoundingBoxHeuristically(e, new Vec3d(0.0D, (double) e.stepHeight, 0.0D),
				bb.expand(movement.x, 0.0D, movement.z), world, ctx, reuseablestream);
			if (vec3d2.y < (double) e.stepHeight) {
				Vec3d vec3d3 = collideBoundingBoxHeuristically(e, new Vec3d(movement.x, 0.0D, movement.z),
					bb.offset(vec3d2), world, ctx, reuseablestream).add(vec3d2);
				if (horizontalMag(vec3d3) > horizontalMag(vec3d1)) {
					vec3d1 = vec3d3;
				}
			}

			if (horizontalMag(vec3d1) > horizontalMag(vec3d)) {
				return vec3d1.add(collideBoundingBoxHeuristically(e, new Vec3d(0.0D, -vec3d1.y + movement.y, 0.0D),
					bb.offset(vec3d1), world, ctx, reuseablestream));
			}
		}

		return vec3d;
	}

	@OnlyIn(Dist.CLIENT)
	private static void checkForClientPlayerCollision(Entity entity) {
		if (entity != Minecraft.getInstance().player)
			return;
		wasClientPlayerGrounded = true;
	}

	public static void pushEntityOutOfShape(Entity entity, VoxelShape voxelShape, Vec3d positionOffset,
		Vec3d shapeMotion) {
		AxisAlignedBB entityBB = entity.getBoundingBox()
			.offset(positionOffset);
		Vec3d entityMotion = entity.getMotion();

		if (!voxelShape.toBoundingBoxList()
			.stream()
			.anyMatch(entityBB::intersects))
			return;

		AxisAlignedBB shapeBB = voxelShape.getBoundingBox();
		Direction bestSide = Direction.DOWN;
		double bestOffset = 100;
		double finalOffset = 0;

		for (Direction face : Direction.values()) {
			Axis axis = face.getAxis();
			double d = axis == Axis.X ? entityBB.getXSize() + shapeBB.getXSize()
				: axis == Axis.Y ? entityBB.getYSize() + shapeBB.getYSize() : entityBB.getZSize() + shapeBB.getZSize();
			d = d + .5f;

			Vec3d nudge = new Vec3d(face.getDirectionVec()).scale(d);
			AxisAlignedBB nudgedBB = entityBB.offset(nudge.getX(), nudge.getY(), nudge.getZ());
			double nudgeDistance = face.getAxisDirection() == AxisDirection.POSITIVE ? -d : d;
			double offset = voxelShape.getAllowedOffset(face.getAxis(), nudgedBB, nudgeDistance);
			double abs = Math.abs(nudgeDistance - offset);
			if (abs < Math.abs(bestOffset) && abs != 0) {
				bestOffset = abs;
				finalOffset = abs;
				bestSide = face;
			}
		}

		if (bestOffset != 0) {
			entity.move(MoverType.SELF, new Vec3d(bestSide.getDirectionVec()).scale(finalOffset));
			boolean positive = bestSide.getAxisDirection() == AxisDirection.POSITIVE;

			double clamped;
			switch (bestSide.getAxis()) {
			case X:
				clamped = positive ? Math.max(shapeMotion.x, entityMotion.x) : Math.min(shapeMotion.x, entityMotion.x);
				entity.setMotion(clamped, entityMotion.y, entityMotion.z);
				break;
			case Y:
				clamped = positive ? Math.max(shapeMotion.y, entityMotion.y) : Math.min(shapeMotion.y, entityMotion.y);
				if (bestSide == Direction.UP)
					clamped = shapeMotion.y;
				entity.setMotion(entityMotion.x, clamped, entityMotion.z);
				entity.handleFallDamage(entity.fallDistance, 1);
				entity.fallDistance = 0;
				entity.onGround = true;
				break;
			case Z:
				clamped = positive ? Math.max(shapeMotion.z, entityMotion.z) : Math.min(shapeMotion.z, entityMotion.z);
				entity.setMotion(entityMotion.x, entityMotion.y, clamped);
				break;
			}
		}
	}

	public static ReuseableStream<VoxelShape> getPotentiallyCollidedShapes(World world, Contraption contraption,
		AxisAlignedBB localBB) {

		double height = localBB.getYSize();
		double width = localBB.getXSize();
		double horizontalFactor = (height > width && width != 0) ? height / width : 1;
		double verticalFactor = (width > height && height != 0) ? width / height : 1;
		AxisAlignedBB blockScanBB = localBB.grow(0.5f);
		blockScanBB = blockScanBB.grow(horizontalFactor, verticalFactor, horizontalFactor);

		BlockPos min = new BlockPos(blockScanBB.minX, blockScanBB.minY, blockScanBB.minZ);
		BlockPos max = new BlockPos(blockScanBB.maxX, blockScanBB.maxY, blockScanBB.maxZ);

		ReuseableStream<VoxelShape> potentialHits = new ReuseableStream<>(BlockPos.getAllInBox(min, max)
			.filter(contraption.blocks::containsKey)
			.map(p -> {
				BlockState blockState = contraption.blocks.get(p).state;
				BlockPos pos = contraption.blocks.get(p).pos;
				VoxelShape collisionShape = blockState.getCollisionShape(world, p);
				return collisionShape.withOffset(pos.getX(), pos.getY(), pos.getZ());
			})
			.filter(Predicates.not(VoxelShape::isEmpty)));

		return potentialHits;
	}

	public static boolean collideBlocks(ContraptionEntity contraptionEntity) {
		if (Contraption.isFrozen())
			return true;
		if (!contraptionEntity.collisionEnabled())
			return false;

		World world = contraptionEntity.getEntityWorld();
		Vec3d motion = contraptionEntity.getMotion();
		Contraption contraption = contraptionEntity.getContraption();
		AxisAlignedBB bounds = contraptionEntity.getBoundingBox();
		Vec3d position = contraptionEntity.getPositionVec();
		BlockPos gridPos = new BlockPos(position);

		if (contraption == null)
			return false;
		if (bounds == null)
			return false;
		if (motion.equals(Vec3d.ZERO))
			return false;

		Direction movementDirection = Direction.getFacingFromVector(motion.x, motion.y, motion.z);

		// Blocks in the world
		if (movementDirection.getAxisDirection() == AxisDirection.POSITIVE)
			gridPos = gridPos.offset(movementDirection);
		if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
			return true;

		// Other moving Contraptions
		for (ContraptionEntity otherContraptionEntity : world.getEntitiesWithinAABB(ContraptionEntity.class,
			bounds.grow(1), e -> !e.equals(contraptionEntity))) {

			if (!otherContraptionEntity.collisionEnabled())
				continue;

			Vec3d otherMotion = otherContraptionEntity.getMotion();
			Contraption otherContraption = otherContraptionEntity.getContraption();
			AxisAlignedBB otherBounds = otherContraptionEntity.getBoundingBox();
			Vec3d otherPosition = otherContraptionEntity.getPositionVec();

			if (otherContraption == null)
				return false;
			if (otherBounds == null)
				return false;

			if (!bounds.offset(motion)
				.intersects(otherBounds.offset(otherMotion)))
				continue;

			for (BlockPos colliderPos : contraption.getColliders(world, movementDirection)) {
				colliderPos = colliderPos.add(gridPos)
					.subtract(new BlockPos(otherPosition));
				if (!otherContraption.blocks.containsKey(colliderPos))
					continue;
				return true;
			}
		}

		return false;
	}

	public static boolean isCollidingWithWorld(World world, Contraption contraption, BlockPos anchor,
		Direction movementDirection) {
		for (BlockPos pos : contraption.getColliders(world, movementDirection)) {
			BlockPos colliderPos = pos.add(anchor);

			if (!world.isBlockPresent(colliderPos))
				return true;

			BlockState collidedState = world.getBlockState(colliderPos);
			BlockInfo blockInfo = contraption.blocks.get(pos);

			if (blockInfo.state.getBlock() instanceof IPortableBlock) {
				IPortableBlock block = (IPortableBlock) blockInfo.state.getBlock();
				if (block.getMovementBehaviour() instanceof BlockBreakingMovementBehaviour) {
					BlockBreakingMovementBehaviour behaviour =
						(BlockBreakingMovementBehaviour) block.getMovementBehaviour();
					if (!behaviour.canBreak(world, colliderPos, collidedState)
						&& !collidedState.getCollisionShape(world, pos)
							.isEmpty()) {
						return true;
					}
					continue;
				}
			}

			if (AllBlocks.PULLEY_MAGNET.has(collidedState) && pos.equals(BlockPos.ZERO)
				&& movementDirection == Direction.UP)
				continue;
			if (collidedState.getBlock() instanceof CocoaBlock)
				continue;
			if (!collidedState.getMaterial()
				.isReplaceable()
				&& !collidedState.getCollisionShape(world, colliderPos)
					.isEmpty()) {
				return true;
			}

		}
		return false;
	}

}
