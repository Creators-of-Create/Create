package com.simibubi.create.content.contraptions.relays.belt.transport;

import static net.minecraft.entity.MoverType.SELF;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BeltMovementHandler {

	public static class TransportedEntityInfo {
		int ticksSinceLastCollision;
		BlockPos lastCollidedPos;
		BlockState lastCollidedState;

		public TransportedEntityInfo(BlockPos collision, BlockState belt) {
			refresh(collision, belt);
		}

		public void refresh(BlockPos collision, BlockState belt) {
			ticksSinceLastCollision = 0;
			lastCollidedPos = new BlockPos(collision).toImmutable();
			lastCollidedState = belt;
		}

		public TransportedEntityInfo tick() {
			ticksSinceLastCollision++;
			return this;
		}
		
		public int getTicksSinceLastCollision() {
			return ticksSinceLastCollision;
		}
	}

	public static boolean canBeTransported(Entity entity) {
		if (!entity.isAlive())
			return false;
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isSneaking())
			return false;
		return true;
	}

	public static void transportEntity(BeltTileEntity beltTe, Entity entityIn, TransportedEntityInfo info) {
		BlockPos pos = info.lastCollidedPos;
		World world = beltTe.getWorld();
		TileEntity te = world.getTileEntity(pos);
		TileEntity tileEntityBelowPassenger = world.getTileEntity(entityIn.getPosition());
		BlockState blockState = info.lastCollidedState;
		Direction movementFacing =
			Direction.getFacingFromAxisDirection(blockState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
					beltTe.getSpeed() < 0 ? POSITIVE : NEGATIVE);

		boolean collidedWithBelt = te instanceof BeltTileEntity;
		boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

		// Don't fight other Belts
		if (!collidedWithBelt || betweenBelts) {
			return;
		}

		// Too slow
		boolean notHorizontal = beltTe.getBlockState().get(BeltBlock.SLOPE) != Slope.HORIZONTAL;
		if (Math.abs(beltTe.getSpeed()) < 1)
			return;

		// Not on top
		if (entityIn.getY() - .25f < pos.getY())
			return;

		// Lock entities in place
		boolean isPlayer = entityIn instanceof PlayerEntity;
		if (entityIn instanceof LivingEntity && !isPlayer) {
			((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 10, 1, false, false));
		}

		BeltTileEntity belt = (BeltTileEntity) te;

		// Attachment pauses movement
		for (BeltAttachmentState state : belt.attachmentTracker.attachments) {
			if (state.attachment.processEntity(belt, entityIn, state)) {
				info.ticksSinceLastCollision--;
				return;
			}
		}

		final Direction beltFacing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
		final Slope slope = blockState.get(BeltBlock.SLOPE);
		final Axis axis = beltFacing.getAxis();
		float movementSpeed = beltTe.getBeltMovementSpeed();
		final Direction movementDirection = Direction.getFacingFromAxis(axis == Axis.X ? NEGATIVE : POSITIVE, axis);

		Vec3i centeringDirection =
			Direction.getFacingFromAxis(POSITIVE, beltFacing.rotateY().getAxis()).getDirectionVec();
		Vec3d movement = new Vec3d(movementDirection.getDirectionVec()).scale(movementSpeed);

		double diffCenter = axis == Axis.Z ? (pos.getX() + .5f - entityIn.getZ()) : (pos.getZ() + .5f - entityIn.getZ());
		if (Math.abs(diffCenter) > 48 / 64f)
			return;

		Part part = blockState.get(BeltBlock.PART);
		float top = 13 / 16f;
		boolean onSlope = notHorizontal && (part == Part.MIDDLE || part == Part.PULLEY
				|| part == (slope == Slope.UPWARD ? Part.END : Part.START) && entityIn.getY() - pos.getY() < top
				|| part == (slope == Slope.UPWARD ? Part.START : Part.END) && entityIn.getY() - pos.getY() > top);

		boolean movingDown = onSlope && slope == (movementFacing == beltFacing ? Slope.DOWNWARD : Slope.UPWARD);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? Slope.UPWARD : Slope.DOWNWARD);

		if (beltFacing.getAxis() == Axis.Z) {
			boolean b = movingDown;
			movingDown = movingUp;
			movingUp = b;
		}

		if (movingUp)
			movement = movement.add(0, Math.abs(axis.getCoordinate(movement.x, movement.y, movement.z)), 0);
		if (movingDown)
			movement = movement.add(0, -Math.abs(axis.getCoordinate(movement.x, movement.y, movement.z)), 0);

		Vec3d centering = new Vec3d(centeringDirection).scale(diffCenter * Math.min(Math.abs(movementSpeed), .1f) * 4);
		movement = movement.add(centering);

		float step = entityIn.stepHeight;
		if (!isPlayer)
			entityIn.stepHeight = 1;

		// Entity Collisions
		if (Math.abs(movementSpeed) < .5f) {
			Vec3d checkDistance = movement.normalize().scale(0.5);
			AxisAlignedBB bb = entityIn.getBoundingBox();
			AxisAlignedBB checkBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
			checkBB = checkBB.offset(checkDistance).grow(-Math.abs(checkDistance.x), -Math.abs(checkDistance.y),
					-Math.abs(checkDistance.z));
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityIn, checkBB);
			list.removeIf(e -> shouldIgnoreBlocking(entityIn, e));
			if (!list.isEmpty()) {
				entityIn.setMotion(0, 0, 0);
				info.ticksSinceLastCollision--;
				return;
			}
		}

		entityIn.fallDistance = 0;

		if (movingUp) {
			float minVelocity = .13f;
			float yMovement = (float) -(Math.max(Math.abs(movement.y), minVelocity));
			entityIn.move(SELF, new Vec3d(0, yMovement, 0));
			entityIn.move(SELF, movement.mul(1, 0, 1));
		} else if (movingDown) {
			entityIn.move(SELF, movement.mul(1, 0, 1));
			entityIn.move(SELF, movement.mul(0, 1, 0));
		} else {
			entityIn.move(SELF, movement);
		}

		if (!isPlayer)
			entityIn.stepHeight = step;

		boolean movedPastEndingSlope = onSlope && (AllBlocks.BELT.has(world.getBlockState(entityIn.getPosition()))
				|| AllBlocks.BELT.has(world.getBlockState(entityIn.getPosition().down())));

		if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
			entityIn.setPosition(entityIn.getY(), entityIn.getY() + movement.y, entityIn.getZ());
		if (movedPastEndingSlope) {
			entityIn.setMotion(movement);
			entityIn.velocityChanged = true;
		}
	}

	public static boolean shouldIgnoreBlocking(Entity me, Entity other) {
		if (other instanceof ContraptionEntity)
			return true;
		if (other instanceof HangingEntity)
			return true;
		return me.isRidingOrBeingRiddenBy(other);
	}

}
