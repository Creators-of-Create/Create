package com.simibubi.create.content.contraptions.components.structureMovement;

import static net.minecraft.entity.Entity.collideBoundingBoxHeuristically;
import static net.minecraft.entity.Entity.horizontalMag;

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
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ContraptionCollider {

	enum PlayerType {
		NONE, CLIENT, REMOTE, SERVER
	}

	static void collideEntities(AbstractContraptionEntity contraptionEntity) {
		World world = contraptionEntity.getEntityWorld();
		Contraption contraption = contraptionEntity.getContraption();
		AxisAlignedBB bounds = contraptionEntity.getBoundingBox();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		Vector3d contraptionPosition = contraptionEntity.getPositionVec();
		Vector3d contraptionMotion = contraptionPosition.subtract(contraptionEntity.getPrevPositionVec());
		Vector3d anchorVec = contraptionEntity.getAnchorVec();
		Vector3d centerOfBlock = VecHelper.CENTER_OF_ORIGIN;
		ContraptionRotationState rotation = null;

		// After death, multiple refs to the client player may show up in the area
		boolean skipClientPlayer = false;

		List<Entity> entitiesWithinAABB = world.getEntitiesWithinAABB(Entity.class, bounds.grow(2)
			.expand(0, 32, 0), contraptionEntity::canCollideWith);
		for (Entity entity : entitiesWithinAABB) {

			PlayerType playerType = getPlayerType(entity);
			if (playerType == PlayerType.REMOTE)
				continue;

			if (playerType == PlayerType.SERVER && entity instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) entity).connection.floatingTickCount = 0;
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
			Vector3d entityPosition = entity.getPositionVec();
			AxisAlignedBB entityBounds = entity.getBoundingBox();
			Vector3d centerY = new Vector3d(0, entityBounds.getYSize() / 2, 0);
			Vector3d motion = entity.getMotion();
			float yawOffset = rotation.getYawOffset();

			Vector3d position = entityPosition;
			position = position.add(centerY);
			position = position.subtract(centerOfBlock);
			position = position.subtract(anchorVec);
			position = VecHelper.rotate(position, -yawOffset, Axis.Y);
			position = rotationMatrix.transform(position);
			position = position.add(centerOfBlock);
			position = position.subtract(centerY);
			position = position.subtract(entityPosition);

			// Find all potential block shapes to collide with
			AxisAlignedBB localBB = entityBounds.offset(position)
				.grow(1.0E-7D);
			ReuseableStream<VoxelShape> potentialHits =
				getPotentiallyCollidedShapes(world, contraption, localBB.expand(motion));
			if (potentialHits.createStream()
				.count() == 0)
				continue;

			// Prepare entity bounds
			OrientedBB obb = new OrientedBB(localBB);
			obb.setRotation(rotationMatrix);
			motion = motion.subtract(contraptionMotion);
			motion = rotationMatrix.transform(motion);

			MutableObject<Vector3d> collisionResponse = new MutableObject<>(Vector3d.ZERO);
			MutableBoolean surfaceCollision = new MutableBoolean(false);
			MutableFloat temporalResponse = new MutableFloat(1);
			Vector3d obbCenter = obb.getCenter();

			// Apply separation maths
			List<AxisAlignedBB> bbs = new ArrayList<>();
			potentialHits.createStream()
				.forEach(shape -> shape.toBoundingBoxList()
					.forEach(bbs::add));

			boolean doHorizontalPass = !rotation.hasVerticalRotation();
			for (boolean horizontalPass : Iterate.trueAndFalse) {
				boolean verticalPass = !horizontalPass || !doHorizontalPass;

				for (AxisAlignedBB bb : bbs) {
					Vector3d currentResponse = collisionResponse.getValue();
					obb.setCenter(obbCenter.add(currentResponse));
					ContinuousSeparationManifold intersect = obb.intersect(bb, motion);

					if (intersect == null)
						continue;
					if (verticalPass && surfaceCollision.isFalse())
						surfaceCollision.setValue(intersect.isSurfaceCollision());

					double timeOfImpact = intersect.getTimeOfImpact();
					if (timeOfImpact > 0 && timeOfImpact < 1) {
						if (temporalResponse.getValue() > timeOfImpact)
							temporalResponse.setValue(timeOfImpact);
						continue;
					}

					Vector3d separation = intersect.asSeparationVec(entity.stepHeight);
					if (separation != null && !separation.equals(Vector3d.ZERO))
						collisionResponse.setValue(currentResponse.add(separation));
				}

				if (verticalPass)
					break;

				boolean noVerticalMotionResponse = temporalResponse.getValue() == 1;
				boolean noVerticalCollision = collisionResponse.getValue().y == 0;
				if (noVerticalCollision && noVerticalMotionResponse)
					break;

				// Re-run collisions with horizontal offset
				collisionResponse.setValue(collisionResponse.getValue()
					.mul(129 / 128f, 0, 129 / 128f));
				continue;
			}

			// Resolve collision
			Vector3d entityMotion = entity.getMotion();
			Vector3d totalResponse = collisionResponse.getValue();
			boolean hardCollision = !totalResponse.equals(Vector3d.ZERO);
			boolean temporalCollision = temporalResponse.getValue() != 1;
			Vector3d motionResponse = !temporalCollision ? motion
				: motion.normalize()
					.scale(motion.length() * temporalResponse.getValue());

			rotationMatrix.transpose();
			motionResponse = rotationMatrix.transform(motionResponse)
				.add(contraptionMotion);
			totalResponse = rotationMatrix.transform(totalResponse);
			totalResponse = VecHelper.rotate(totalResponse, yawOffset, Axis.Y);
			rotationMatrix.transpose();

			if (temporalCollision) {
				double idealVerticalMotion = motionResponse.y;
				if (idealVerticalMotion != entityMotion.y) {
					entity.setMotion(entityMotion.mul(1, 0, 1)
						.add(0, idealVerticalMotion, 0));
					entityMotion = entity.getMotion();
				}
			}

			if (hardCollision) {
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
					entityMotion = entityMotion.mul(1, 0, 1)
						.add(0, contraptionMotion.y, 0);
				if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
					entityMotion = entityMotion.mul(1, 1, 0);
			}

			if (!hardCollision && surfaceCollision.isFalse())
				continue;

			Vector3d allowedMovement = getAllowedMovement(totalResponse, entity);
			entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y,
				entityPosition.z + allowedMovement.z);
			entityPosition = entity.getPositionVec();

			entity.velocityChanged = true;
			Vector3d contactPointMotion = Vector3d.ZERO;

			if (surfaceCollision.isTrue()) {
				entity.fallDistance = 0;
				entity.setOnGround(true);
				contraptionEntity.collidingEntities.put(entity, new MutableInt(0));
				if (entity instanceof ItemEntity)
					entityMotion = entityMotion.mul(.5f, 1, .5f);
				contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
				allowedMovement = getAllowedMovement(contactPointMotion, entity);
				entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y,
					entityPosition.z + allowedMovement.z);
			}

			entity.setMotion(entityMotion);

			if (playerType != PlayerType.CLIENT)
				continue;

			double d0 = entity.getX() - entity.prevPosX - contactPointMotion.x;
			double d1 = entity.getZ() - entity.prevPosZ - contactPointMotion.z;
			float limbSwing = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
			if (limbSwing > 1.0F)
				limbSwing = 1.0F;
			AllPackets.channel.sendToServer(new ClientMotionPacket(entityMotion, true, limbSwing));
		}

	}

	/** From Entity#getAllowedMovement **/
	static Vector3d getAllowedMovement(Vector3d movement, Entity e) {
		AxisAlignedBB bb = e.getBoundingBox();
		ISelectionContext ctx = ISelectionContext.forEntity(e);
		World world = e.world;
		VoxelShape voxelshape = world.getWorldBorder()
			.getShape();
		Stream<VoxelShape> stream =
			VoxelShapes.compare(voxelshape, VoxelShapes.create(bb.shrink(1.0E-7D)), IBooleanFunction.AND)
				? Stream.empty()
				: Stream.of(voxelshape);
		Stream<VoxelShape> stream1 = world.getEntityCollisions(e, bb.expand(movement), entity -> false); // FIXME: 1.15 equivalent translated correctly?
		ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
		Vector3d Vector3d = movement.lengthSquared() == 0.0D ? movement
			: collideBoundingBoxHeuristically(e, movement, bb, world, ctx, reuseablestream);
		boolean flag = movement.x != Vector3d.x;
		boolean flag1 = movement.y != Vector3d.y;
		boolean flag2 = movement.z != Vector3d.z;
		boolean flag3 = e.isOnGround() || flag1 && movement.y < 0.0D;
		if (e.stepHeight > 0.0F && flag3 && (flag || flag2)) {
			Vector3d Vector3d1 = collideBoundingBoxHeuristically(e, new Vector3d(movement.x, (double) e.stepHeight, movement.z),
				bb, world, ctx, reuseablestream);
			Vector3d Vector3d2 = collideBoundingBoxHeuristically(e, new Vector3d(0.0D, (double) e.stepHeight, 0.0D),
				bb.expand(movement.x, 0.0D, movement.z), world, ctx, reuseablestream);
			if (Vector3d2.y < (double) e.stepHeight) {
				Vector3d Vector3d3 = collideBoundingBoxHeuristically(e, new Vector3d(movement.x, 0.0D, movement.z),
					bb.offset(Vector3d2), world, ctx, reuseablestream).add(Vector3d2);
				if (horizontalMag(Vector3d3) > horizontalMag(Vector3d1)) {
					Vector3d1 = Vector3d3;
				}
			}

			if (horizontalMag(Vector3d1) > horizontalMag(Vector3d)) {
				return Vector3d1.add(collideBoundingBoxHeuristically(e, new Vector3d(0.0D, -Vector3d1.y + movement.y, 0.0D),
					bb.offset(Vector3d1), world, ctx, reuseablestream));
			}
		}

		return Vector3d;
	}

	private static PlayerType getPlayerType(Entity entity) {
		if (!(entity instanceof PlayerEntity))
			return PlayerType.NONE;
		if (!entity.world.isRemote)
			return PlayerType.SERVER;
		MutableBoolean isClient = new MutableBoolean(false);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> isClient.setValue(isClientPlayerEntity(entity)));
		return isClient.booleanValue() ? PlayerType.CLIENT : PlayerType.REMOTE;
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isClientPlayerEntity(Entity entity) {
		return entity instanceof ClientPlayerEntity;
	}

	private static ReuseableStream<VoxelShape> getPotentiallyCollidedShapes(World world, Contraption contraption,
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
			.filter(contraption.getBlocks()::containsKey)
			.map(p -> {
				BlockState blockState = contraption.getBlocks()
					.get(p).state;
				BlockPos pos = contraption.getBlocks()
					.get(p).pos;
				VoxelShape collisionShape = blockState.getCollisionShape(world, p);
				return collisionShape.withOffset(pos.getX(), pos.getY(), pos.getZ());
			})
			.filter(Predicates.not(VoxelShape::isEmpty)));

		return potentialHits;
	}

	public static boolean collideBlocks(ControlledContraptionEntity contraptionEntity) {
		if (!contraptionEntity.supportsTerrainCollision())
			return false;

		World world = contraptionEntity.getEntityWorld();
		Vector3d motion = contraptionEntity.getMotion();
		TranslatingContraption contraption = (TranslatingContraption) contraptionEntity.getContraption();
		AxisAlignedBB bounds = contraptionEntity.getBoundingBox();
		Vector3d position = contraptionEntity.getPositionVec();
		BlockPos gridPos = new BlockPos(position);

		if (contraption == null)
			return false;
		if (bounds == null)
			return false;
		if (motion.equals(Vector3d.ZERO))
			return false;

		Direction movementDirection = Direction.getFacingFromVector(motion.x, motion.y, motion.z);

		// Blocks in the world
		if (movementDirection.getAxisDirection() == AxisDirection.POSITIVE)
			gridPos = gridPos.offset(movementDirection);
		if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
			return true;

		// Other moving Contraptions
		for (ControlledContraptionEntity otherContraptionEntity : world.getEntitiesWithinAABB(
			ControlledContraptionEntity.class, bounds.grow(1), e -> !e.equals(contraptionEntity))) {

			if (!otherContraptionEntity.supportsTerrainCollision())
				continue;

			Vector3d otherMotion = otherContraptionEntity.getMotion();
			TranslatingContraption otherContraption = (TranslatingContraption) otherContraptionEntity.getContraption();
			AxisAlignedBB otherBounds = otherContraptionEntity.getBoundingBox();
			Vector3d otherPosition = otherContraptionEntity.getPositionVec();

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
				if (!otherContraption.getBlocks()
					.containsKey(colliderPos))
					continue;
				return true;
			}
		}

		return false;
	}

	public static boolean isCollidingWithWorld(World world, TranslatingContraption contraption, BlockPos anchor,
		Direction movementDirection) {
		for (BlockPos pos : contraption.getColliders(world, movementDirection)) {
			BlockPos colliderPos = pos.add(anchor);

			if (!world.isBlockPresent(colliderPos))
				return true;

			BlockState collidedState = world.getBlockState(colliderPos);
			BlockInfo blockInfo = contraption.getBlocks()
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
