package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.components.actors.BlockBreakingMovementBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ContraptionCollider {

	static Map<Object, AxisAlignedBB> renderedBBs = new HashMap<>();
	public static boolean wasClientPlayerGrounded;

	public static void collideEntities(ContraptionEntity contraptionEntity) {
		if (Contraption.isFrozen())
			return;
		if (!contraptionEntity.collisionEnabled())
			return;

		World world = contraptionEntity.getEntityWorld();
		Vec3d contraptionMotion = contraptionEntity.getMotion();
		Contraption contraption = contraptionEntity.getContraption();
		AxisAlignedBB bounds = contraptionEntity.getBoundingBox();
		Vec3d contraptionPosition = contraptionEntity.getPositionVec();
		contraptionEntity.collidingEntities.clear();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		for (Entity entity : world.getEntitiesWithinAABB((EntityType<?>) null, bounds.grow(1),
				e -> canBeCollidedWith(e))) {

			ReuseableStream<VoxelShape> potentialHits =
				getPotentiallyCollidedShapes(world, contraption, contraptionPosition, entity);
			if (potentialHits.createStream().count() == 0)
				continue;

			Vec3d positionOffset = contraptionPosition.scale(-1);
			AxisAlignedBB entityBB = entity.getBoundingBox().offset(positionOffset).grow(1.0E-7D);
			Vec3d entityMotion = entity.getMotion();
			Vec3d relativeMotion = entityMotion.subtract(contraptionMotion);
			Vec3d allowedMovement = Entity.getAllowedMovement(relativeMotion, entityBB, world,
					ISelectionContext.forEntity(entity), potentialHits);
			potentialHits.createStream()
					.forEach(voxelShape -> pushEntityOutOfShape(entity, voxelShape, positionOffset, contraptionMotion));

			contraptionEntity.collidingEntities.add(entity);

			if (allowedMovement.equals(relativeMotion))
				continue;
			
			if (allowedMovement.y != relativeMotion.y) {
				entity.fall(entity.fallDistance, 1);
				entity.fallDistance = 0;
				entity.onGround = true;
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> checkForClientPlayerCollision(entity));
			}

			if (entity instanceof ServerPlayerEntity)
				((ServerPlayerEntity) entity).connection.floatingTickCount = 0;
			if (entity instanceof PlayerEntity && !world.isRemote)
				return;

			entity.setMotion(allowedMovement.add(contraptionMotion));
			entity.velocityChanged = true;
		}

	}

	public static boolean canBeCollidedWith(Entity e) {
		if (e instanceof PlayerEntity && e.isSpectator())
			return false;
		if (e.noClip)
			return false;
		if (e instanceof IProjectile)
			return false;
		return e.getPushReaction() == PushReaction.NORMAL;
	}

	@OnlyIn(Dist.CLIENT)
	private static void checkForClientPlayerCollision(Entity entity) {
		if (entity != Minecraft.getInstance().player)
			return;
		wasClientPlayerGrounded = true;
	}

	public static void pushEntityOutOfShape(Entity entity, VoxelShape voxelShape, Vec3d positionOffset,
			Vec3d shapeMotion) {
		AxisAlignedBB entityBB = entity.getBoundingBox().offset(positionOffset);
		Vec3d entityMotion = entity.getMotion();

		if (!voxelShape.toBoundingBoxList().stream().anyMatch(entityBB::intersects))
			return;

		AxisAlignedBB shapeBB = voxelShape.getBoundingBox();
		Direction bestSide = Direction.DOWN;
		double bestOffset = 100;
		double finalOffset = 0;

		for (Direction face : Direction.values()) {
			Axis axis = face.getAxis();
			double d = axis == Axis.X ? entityBB.getXSize() + shapeBB.getXSize()
					: axis == Axis.Y ? entityBB.getYSize() + shapeBB.getYSize()
							: entityBB.getZSize() + shapeBB.getZSize();
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
				entity.fall(entity.fallDistance, 1);
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
			Vec3d contraptionPosition, Entity entity) {
		AxisAlignedBB blockScanBB = entity.getBoundingBox().offset(contraptionPosition.scale(-1)).grow(.5f);
		BlockPos min = new BlockPos(blockScanBB.minX, blockScanBB.minY, blockScanBB.minZ);
		BlockPos max = new BlockPos(blockScanBB.maxX, blockScanBB.maxY, blockScanBB.maxZ);

		ReuseableStream<VoxelShape> potentialHits =
			new ReuseableStream<>(BlockPos.getAllInBox(min, max).filter(contraption.blocks::containsKey).map(p -> {
				BlockState blockState = contraption.blocks.get(p).state;
				BlockPos pos = contraption.blocks.get(p).pos;
				VoxelShape collisionShape = blockState.getCollisionShape(world, p);
				return collisionShape.withOffset(pos.getX(), pos.getY(), pos.getZ());
			}));

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

			if (!bounds.offset(motion).intersects(otherBounds.offset(otherMotion)))
				continue;

			for (BlockPos colliderPos : contraption.getColliders(world, movementDirection)) {
				colliderPos = colliderPos.add(gridPos).subtract(new BlockPos(otherPosition));
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
							&& !collidedState.getCollisionShape(world, pos).isEmpty()) {
						return true;
					}
					continue;
				}
			}

			if (AllBlocks.PULLEY_MAGNET.typeOf(collidedState) && pos.equals(BlockPos.ZERO)
					&& movementDirection == Direction.UP)
				continue;

			if (!collidedState.getMaterial().isReplaceable()
					&& !collidedState.getCollisionShape(world, colliderPos).isEmpty()) {
				return true;
			}

		}
		return false;
	}

}
