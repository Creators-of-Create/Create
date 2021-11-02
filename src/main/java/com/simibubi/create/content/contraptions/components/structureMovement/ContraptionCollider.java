package com.simibubi.create.content.contraptions.components.structureMovement;

import static net.minecraft.entity.Entity.collideBoundingBoxHeuristically;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.actors.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ClientMotionPacket;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.collision.OrientedBB;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ContraptionCollider {

	enum PlayerType {
		NONE, CLIENT, REMOTE, SERVER
	}

	static void collideEntities(AbstractContraptionEntity contraptionEntity) {
		Level world = contraptionEntity.getCommandSenderWorld();
		Contraption contraption = contraptionEntity.getContraption();
		AABB bounds = contraptionEntity.getBoundingBox();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		Vec3 contraptionPosition = contraptionEntity.position();
		Vec3 contraptionMotion = contraptionPosition.subtract(contraptionEntity.getPrevPositionVec());
		Vec3 anchorVec = contraptionEntity.getAnchorVec();
		ContraptionRotationState rotation = null;

		// After death, multiple refs to the client player may show up in the area
		boolean skipClientPlayer = false;

		List<Entity> entitiesWithinAABB = world.getEntitiesOfClass(Entity.class, bounds.inflate(2)
			.expandTowards(0, 32, 0), contraptionEntity::canCollideWith);
		for (Entity entity : entitiesWithinAABB) {

			PlayerType playerType = getPlayerType(entity);
			if (playerType == PlayerType.REMOTE)
				continue;

			if (playerType == PlayerType.SERVER && entity instanceof ServerPlayer) {
				((ServerPlayer) entity).connection.aboveGroundTickCount = 0;
				continue;
			}

			if (playerType == PlayerType.CLIENT)
				if (skipClientPlayer)
					continue;
				else
					skipClientPlayer = true;

			// Init matrix
			if (rotation == null)
				rotation = contraptionEntity.getRotationState();
			Matrix3d rotationMatrix = rotation.asMatrix();

			// Transform entity position and motion to local space
			Vec3 entityPosition = entity.position();
			AABB entityBounds = entity.getBoundingBox();
			Vec3 motion = entity.getDeltaMovement();
			float yawOffset = rotation.getYawOffset();
			Vec3 position = getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);

			// Prepare entity bounds
			AABB localBB = entityBounds.move(position)
				.inflate(1.0E-7D);
			OrientedBB obb = new OrientedBB(localBB);
			obb.setRotation(rotationMatrix);
			motion = motion.subtract(contraptionMotion);
			motion = rotationMatrix.transform(motion);

			// Use simplified bbs when present
			final Vec3 motionCopy = motion;
			List<AABB> collidableBBs = contraption.simplifiedEntityColliders.orElseGet(() -> {

				// Else find 'nearby' individual block shapes to collide with
				List<AABB> bbs = new ArrayList<>();
				RewindableStream<VoxelShape> potentialHits =
					getPotentiallyCollidedShapes(world, contraption, localBB.expandTowards(motionCopy));
				potentialHits.getStream()
					.forEach(shape -> shape.toAabbs()
						.forEach(bbs::add));
				return bbs;

			});

			MutableObject<Vec3> collisionResponse = new MutableObject<>(Vec3.ZERO);
			MutableObject<Vec3> normal = new MutableObject<>(Vec3.ZERO);
			MutableObject<Vec3> location = new MutableObject<>(Vec3.ZERO);
			MutableBoolean surfaceCollision = new MutableBoolean(false);
			MutableFloat temporalResponse = new MutableFloat(1);
			Vec3 obbCenter = obb.getCenter();

			// Apply separation maths
			boolean doHorizontalPass = !rotation.hasVerticalRotation();
			for (boolean horizontalPass : Iterate.trueAndFalse) {
				boolean verticalPass = !horizontalPass || !doHorizontalPass;

				for (AABB bb : collidableBBs) {
					Vec3 currentResponse = collisionResponse.getValue();
					Vec3 currentCenter = obbCenter.add(currentResponse);

					if (Math.abs(currentCenter.x - bb.getCenter().x) - entityBounds.getXsize() - 1 > bb.getXsize() / 2)
						continue;
					if (Math.abs((currentCenter.y + motion.y) - bb.getCenter().y) - entityBounds.getYsize()
						- 1 > bb.getYsize() / 2)
						continue;
					if (Math.abs(currentCenter.z - bb.getCenter().z) - entityBounds.getZsize() - 1 > bb.getZsize() / 2)
						continue;

					obb.setCenter(currentCenter);
					ContinuousSeparationManifold intersect = obb.intersect(bb, motion);

					if (intersect == null)
						continue;
					if (verticalPass && surfaceCollision.isFalse())
						surfaceCollision.setValue(intersect.isSurfaceCollision());

					double timeOfImpact = intersect.getTimeOfImpact();
					boolean isTemporal = timeOfImpact > 0 && timeOfImpact < 1;
					Vec3 collidingNormal = intersect.getCollisionNormal();
					Vec3 collisionPosition = intersect.getCollisionPosition();

					if (!isTemporal) {
						Vec3 separation = intersect.asSeparationVec(entity.maxUpStep);
						if (separation != null && !separation.equals(Vec3.ZERO)) {
							collisionResponse.setValue(currentResponse.add(separation));
							timeOfImpact = 0;
						}
					}

					boolean nearest = timeOfImpact >= 0 && temporalResponse.getValue() > timeOfImpact;
					if (collidingNormal != null && nearest)
						normal.setValue(collidingNormal);
					if (collisionPosition != null && nearest)
						location.setValue(collisionPosition);

					if (isTemporal) {
						if (temporalResponse.getValue() > timeOfImpact)
							temporalResponse.setValue(timeOfImpact);
					}
				}

				if (verticalPass)
					break;

				boolean noVerticalMotionResponse = temporalResponse.getValue() == 1;
				boolean noVerticalCollision = collisionResponse.getValue().y == 0;
				if (noVerticalCollision && noVerticalMotionResponse)
					break;

				// Re-run collisions with horizontal offset
				collisionResponse.setValue(collisionResponse.getValue()
					.multiply(129 / 128f, 0, 129 / 128f));
				continue;
			}

			// Resolve collision
			Vec3 entityMotion = entity.getDeltaMovement();
			Vec3 entityMotionNoTemporal = entityMotion;
			Vec3 collisionNormal = normal.getValue();
			Vec3 collisionLocation = location.getValue();
			Vec3 totalResponse = collisionResponse.getValue();
			boolean hardCollision = !totalResponse.equals(Vec3.ZERO);
			boolean temporalCollision = temporalResponse.getValue() != 1;
			Vec3 motionResponse = !temporalCollision ? motion
				: motion.normalize()
					.scale(motion.length() * temporalResponse.getValue());

			rotationMatrix.transpose();
			motionResponse = rotationMatrix.transform(motionResponse)
				.add(contraptionMotion);
			totalResponse = rotationMatrix.transform(totalResponse);
			totalResponse = VecHelper.rotate(totalResponse, yawOffset, Axis.Y);
			collisionNormal = rotationMatrix.transform(collisionNormal);
			collisionNormal = VecHelper.rotate(collisionNormal, yawOffset, Axis.Y);
			collisionNormal = collisionNormal.normalize();
			collisionLocation = rotationMatrix.transform(collisionLocation);
			collisionLocation = VecHelper.rotate(collisionLocation, yawOffset, Axis.Y);
			rotationMatrix.transpose();

			double bounce = 0;
			double slide = 0;

			if (!collisionLocation.equals(Vec3.ZERO)) {
				collisionLocation = collisionLocation.add(entity.position()
					.add(entity.getBoundingBox()
						.getCenter())
					.scale(.5f));
				if (temporalCollision)
					collisionLocation = collisionLocation.add(0, motionResponse.y, 0);
				BlockPos pos = new BlockPos(contraptionEntity.toLocalVector(collisionLocation, 0));
				if (contraption.getBlocks()
					.containsKey(pos)) {
					BlockState blockState = contraption.getBlocks()
						.get(pos).state;
					bounce = BlockHelper.getBounceMultiplier(blockState.getBlock());
					slide = Math.max(0, blockState.getSlipperiness(contraption.world, pos, entity) - .6f);
				}
			}

			boolean hasNormal = !collisionNormal.equals(Vec3.ZERO);
			boolean anyCollision = hardCollision || temporalCollision;

			if (bounce > 0 && hasNormal && anyCollision && bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
				entity.level.playSound(playerType == PlayerType.CLIENT ? (Player) entity : null,
					entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_BLOCK_FALL,
					SoundSource.BLOCKS, .5f, 1);
				continue;
			}

			if (temporalCollision) {
				double idealVerticalMotion = motionResponse.y;
				if (idealVerticalMotion != entityMotion.y) {
					entity.setDeltaMovement(entityMotion.multiply(1, 0, 1)
						.add(0, idealVerticalMotion, 0));
					entityMotion = entity.getDeltaMovement();
				}
			}

			if (hardCollision) {
				double motionX = entityMotion.x();
				double motionY = entityMotion.y();
				double motionZ = entityMotion.z();
				double intersectX = totalResponse.x();
				double intersectY = totalResponse.y();
				double intersectZ = totalResponse.z();

				double horizonalEpsilon = 1 / 128f;
				if (motionX != 0 && Math.abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
					entityMotion = entityMotion.multiply(0, 1, 1);
				if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
					entityMotion = entityMotion.multiply(1, 0, 1)
						.add(0, contraptionMotion.y, 0);
				if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
					entityMotion = entityMotion.multiply(1, 1, 0);
			}

			if (bounce == 0 && slide > 0 && hasNormal && anyCollision && rotation.hasVerticalRotation()) {
				double slideFactor = collisionNormal.multiply(1, 0, 1)
					.length() * 1.25f;
				Vec3 motionIn = entityMotionNoTemporal.multiply(0, .9, 0)
					.add(0, -.01f, 0);
				Vec3 slideNormal = collisionNormal.cross(motionIn.cross(collisionNormal))
					.normalize();
				Vec3 newMotion = entityMotion.multiply(.85, 0, .85)
					.add(slideNormal.scale((.2f + slide) * motionIn.length() * slideFactor)
						.add(0, -.1f - collisionNormal.y * .125f, 0));
				entity.setDeltaMovement(newMotion);
				entityMotion = entity.getDeltaMovement();
			}

			if (!hardCollision && surfaceCollision.isFalse())
				continue;

			Vec3 allowedMovement = getAllowedMovement(totalResponse, entity);
			entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y,
				entityPosition.z + allowedMovement.z);
			entityPosition = entity.position();

			entity.hurtMarked = true;
			Vec3 contactPointMotion = Vec3.ZERO;

			if (surfaceCollision.isTrue()) {
				entity.fallDistance = 0;
				contraptionEntity.collidingEntities.put(entity, new MutableInt(0));
				boolean canWalk = bounce != 0 || slide == 0;
				if (canWalk || !rotation.hasVerticalRotation()) {
					if (canWalk)
						entity.onGround = true;
					if (entity instanceof ItemEntity)
						entityMotion = entityMotion.multiply(.5f, 1, .5f);
				}
				contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
				allowedMovement = getAllowedMovement(contactPointMotion, entity);
				entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y,
					entityPosition.z + allowedMovement.z);
			}

			entity.setDeltaMovement(entityMotion);

			if (playerType != PlayerType.CLIENT)
				continue;

			double d0 = entity.getX() - entity.xo - contactPointMotion.x;
			double d1 = entity.getZ() - entity.zo - contactPointMotion.z;
			float limbSwing = Mth.sqrt(d0 * d0 + d1 * d1) * 4.0F;
			if (limbSwing > 1.0F)
				limbSwing = 1.0F;
			AllPackets.channel.sendToServer(new ClientMotionPacket(entityMotion, true, limbSwing));
		}

	}

	static boolean bounceEntity(Entity entity, Vec3 normal, AbstractContraptionEntity contraption, double factor) {
		if (factor == 0)
			return false;
		if (entity.isSuppressingBounce())
			return false;

		Vec3 contactPointMotion = contraption.getContactPointMotion(entity.position());
		Vec3 motion = entity.getDeltaMovement().subtract(contactPointMotion);
		Vec3 deltav = normal.scale(factor*2*motion.dot(normal));
		if (deltav.dot(deltav) < 0.1f)
		 	return false;
		entity.setDeltaMovement(entity.getDeltaMovement().subtract(deltav));
		return true;
	}

	public static Vec3 getWorldToLocalTranslation(Entity entity, AbstractContraptionEntity contraptionEntity) {
		return getWorldToLocalTranslation(entity, contraptionEntity.getAnchorVec(), contraptionEntity.getRotationState());
	}

	public static Vec3 getWorldToLocalTranslation(Entity entity, Vec3 anchorVec, ContraptionRotationState rotation) {
		return getWorldToLocalTranslation(entity, anchorVec, rotation.asMatrix(), rotation.getYawOffset());
	}

	public static Vec3 getWorldToLocalTranslation(Entity entity, Vec3 anchorVec, Matrix3d rotationMatrix, float yawOffset) {
		Vec3 entityPosition = entity.position();
		Vec3 centerY = new Vec3(0, entity.getBoundingBox().getYsize() / 2, 0);
		Vec3 position = entityPosition;
		position = position.add(centerY);
		position = position.subtract(VecHelper.CENTER_OF_ORIGIN);
		position = position.subtract(anchorVec);
		position = VecHelper.rotate(position, -yawOffset, Axis.Y);
		position = rotationMatrix.transform(position);
		position = position.add(VecHelper.CENTER_OF_ORIGIN);
		position = position.subtract(centerY);
		position = position.subtract(entityPosition);
		return position;
	}

	public static Vec3 getWorldToLocalTranslation(Vec3 entity, AbstractContraptionEntity contraptionEntity) {
		return getWorldToLocalTranslation(entity, contraptionEntity.getAnchorVec(), contraptionEntity.getRotationState());
	}

	public static Vec3 getWorldToLocalTranslation(Vec3 inPos, Vec3 anchorVec, ContraptionRotationState rotation) {
		return getWorldToLocalTranslation(inPos, anchorVec, rotation.asMatrix(), rotation.getYawOffset());
	}

	public static Vec3 getWorldToLocalTranslation(Vec3 inPos, Vec3 anchorVec, Matrix3d rotationMatrix, float yawOffset) {
		Vec3 position = inPos;
		position = position.subtract(VecHelper.CENTER_OF_ORIGIN);
		position = position.subtract(anchorVec);
		position = VecHelper.rotate(position, -yawOffset, Axis.Y);
		position = rotationMatrix.transform(position);
		position = position.add(VecHelper.CENTER_OF_ORIGIN);
		position = position.subtract(inPos);
		return position;
	}

	/** From Entity#getAllowedMovement **/
	static Vec3 getAllowedMovement(Vec3 movement, Entity e) {
		AABB bb = e.getBoundingBox();
		CollisionContext ctx = CollisionContext.of(e);
		Level world = e.level;
		VoxelShape voxelshape = world.getWorldBorder()
			.getCollisionShape();
		Stream<VoxelShape> stream =
			Shapes.joinIsNotEmpty(voxelshape, Shapes.create(bb.deflate(1.0E-7D)), BooleanOp.AND)
				? Stream.empty()
				: Stream.of(voxelshape);
		Stream<VoxelShape> stream1 = world.getEntityCollisions(e, bb.expandTowards(movement), entity -> false); // FIXME: 1.15 equivalent translated correctly?
		RewindableStream<VoxelShape> reuseablestream = new RewindableStream<>(Stream.concat(stream1, stream));
		Vec3 allowedMovement = movement.lengthSqr() == 0.0D ? movement
			: collideBoundingBoxHeuristically(e, movement, bb, world, ctx, reuseablestream);
		boolean xDifferent = movement.x != allowedMovement.x;
		boolean yDifferent = movement.y != allowedMovement.y;
		boolean zDifferent = movement.z != allowedMovement.z;
		boolean notMovingUp = e.isOnGround() || yDifferent && movement.y < 0.0D;
		if (e.maxUpStep > 0.0F && notMovingUp && (xDifferent || zDifferent)) {
			Vec3 allowedStep = collideBoundingBoxHeuristically(e, new Vec3(movement.x, (double) e.maxUpStep, movement.z),
				bb, world, ctx, reuseablestream);
			Vec3 allowedStepGivenMovement = collideBoundingBoxHeuristically(e, new Vec3(0.0D, (double) e.maxUpStep, 0.0D),
				bb.expandTowards(movement.x, 0.0D, movement.z), world, ctx, reuseablestream);
			if (allowedStepGivenMovement.y < (double) e.maxUpStep) {
				Vec3 vec3 = collideBoundingBoxHeuristically(e, new Vec3(movement.x, 0.0D, movement.z),
					bb.move(allowedStepGivenMovement), world, ctx, reuseablestream).add(allowedStepGivenMovement);
				if (getHorizontalDistanceSqr(vec3) > getHorizontalDistanceSqr(allowedStep)) {
					allowedStep = vec3;
				}
			}

			if (getHorizontalDistanceSqr(allowedStep) > getHorizontalDistanceSqr(allowedMovement)) {
				return allowedStep.add(collideBoundingBoxHeuristically(e, new Vec3(0.0D, -allowedStep.y + movement.y, 0.0D),
					bb.move(allowedStep), world, ctx, reuseablestream));
			}
		}

		return allowedMovement;
	}

	private static PlayerType getPlayerType(Entity entity) {
		if (!(entity instanceof Player))
			return PlayerType.NONE;
		if (!entity.level.isClientSide)
			return PlayerType.SERVER;
		MutableBoolean isClient = new MutableBoolean(false);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> isClient.setValue(isClientPlayerEntity(entity)));
		return isClient.booleanValue() ? PlayerType.CLIENT : PlayerType.REMOTE;
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isClientPlayerEntity(Entity entity) {
		return entity instanceof LocalPlayer;
	}

	private static RewindableStream<VoxelShape> getPotentiallyCollidedShapes(Level world, Contraption contraption,
		AABB localBB) {

		double height = localBB.getYsize();
		double width = localBB.getXsize();
		double horizontalFactor = (height > width && width != 0) ? height / width : 1;
		double verticalFactor = (width > height && height != 0) ? width / height : 1;
		AABB blockScanBB = localBB.inflate(0.5f);
		blockScanBB = blockScanBB.inflate(horizontalFactor, verticalFactor, horizontalFactor);

		BlockPos min = new BlockPos(blockScanBB.minX, blockScanBB.minY, blockScanBB.minZ);
		BlockPos max = new BlockPos(blockScanBB.maxX, blockScanBB.maxY, blockScanBB.maxZ);

		RewindableStream<VoxelShape> potentialHits = new RewindableStream<>(BlockPos.betweenClosedStream(min, max)
			.filter(contraption.getBlocks()::containsKey)
			.map(p -> {
				BlockState blockState = contraption.getBlocks()
					.get(p).state;
				BlockPos pos = contraption.getBlocks()
					.get(p).pos;
				VoxelShape collisionShape = blockState.getCollisionShape(world, p);
				return collisionShape.move(pos.getX(), pos.getY(), pos.getZ());
			})
			.filter(Predicates.not(VoxelShape::isEmpty)));

		return potentialHits;
	}

	public static boolean collideBlocks(AbstractContraptionEntity contraptionEntity) {
		if (!contraptionEntity.supportsTerrainCollision())
			return false;

		Level world = contraptionEntity.getCommandSenderWorld();
		Vec3 motion = contraptionEntity.getDeltaMovement();
		TranslatingContraption contraption = (TranslatingContraption) contraptionEntity.getContraption();
		AABB bounds = contraptionEntity.getBoundingBox();
		Vec3 position = contraptionEntity.position();
		BlockPos gridPos = new BlockPos(position);

		if (contraption == null)
			return false;
		if (bounds == null)
			return false;
		if (motion.equals(Vec3.ZERO))
			return false;

		Direction movementDirection = Direction.getNearest(motion.x, motion.y, motion.z);

		// Blocks in the world
		if (movementDirection.getAxisDirection() == AxisDirection.POSITIVE)
			gridPos = gridPos.relative(movementDirection);
		if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
			return true;

		// Other moving Contraptions
		for (ControlledContraptionEntity otherContraptionEntity : world.getEntitiesOfClass(
			ControlledContraptionEntity.class, bounds.inflate(1), e -> !e.equals(contraptionEntity))) {

			if (!otherContraptionEntity.supportsTerrainCollision())
				continue;

			Vec3 otherMotion = otherContraptionEntity.getDeltaMovement();
			TranslatingContraption otherContraption = (TranslatingContraption) otherContraptionEntity.getContraption();
			AABB otherBounds = otherContraptionEntity.getBoundingBox();
			Vec3 otherPosition = otherContraptionEntity.position();

			if (otherContraption == null)
				return false;
			if (otherBounds == null)
				return false;

			if (!bounds.move(motion)
				.intersects(otherBounds.move(otherMotion)))
				continue;

			for (BlockPos colliderPos : contraption.getColliders(world, movementDirection)) {
				colliderPos = colliderPos.offset(gridPos)
					.subtract(new BlockPos(otherPosition));
				if (!otherContraption.getBlocks()
					.containsKey(colliderPos))
					continue;
				return true;
			}
		}

		return false;
	}

	public static boolean isCollidingWithWorld(Level world, TranslatingContraption contraption, BlockPos anchor,
		Direction movementDirection) {
		for (BlockPos pos : contraption.getColliders(world, movementDirection)) {
			BlockPos colliderPos = pos.offset(anchor);

			if (!world.isLoaded(colliderPos))
				return true;

			BlockState collidedState = world.getBlockState(colliderPos);
			StructureBlockInfo blockInfo = contraption.getBlocks()
				.get(pos);

			if (AllMovementBehaviours.contains(blockInfo.state.getBlock())) {
				MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state.getBlock());
				if (movementBehaviour instanceof BlockBreakingMovementBehaviour) {
					BlockBreakingMovementBehaviour behaviour = (BlockBreakingMovementBehaviour) movementBehaviour;
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
