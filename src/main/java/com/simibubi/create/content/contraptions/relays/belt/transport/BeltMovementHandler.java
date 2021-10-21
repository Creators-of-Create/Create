package com.simibubi.create.content.contraptions.relays.belt.transport;

import static net.minecraft.entity.MoverType.SELF;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;

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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
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
			lastCollidedPos = new BlockPos(collision).immutable();
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
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isShiftKeyDown())
			return false;
		return true;
	}

	public static void transportEntity(BeltTileEntity beltTe, Entity entityIn, TransportedEntityInfo info) {
		BlockPos pos = info.lastCollidedPos;
		World world = beltTe.getLevel();
		TileEntity te = world.getBlockEntity(pos);
		TileEntity tileEntityBelowPassenger = world.getBlockEntity(entityIn.blockPosition());
		BlockState blockState = info.lastCollidedState;
		Direction movementFacing =
			Direction.fromAxisAndDirection(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
				.getAxis(), beltTe.getSpeed() < 0 ? POSITIVE : NEGATIVE);

		boolean collidedWithBelt = te instanceof BeltTileEntity;
		boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

		// Don't fight other Belts
		if (!collidedWithBelt || betweenBelts) {
			return;
		}

		// Too slow
		boolean notHorizontal = beltTe.getBlockState()
			.getValue(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL;
		if (Math.abs(beltTe.getSpeed()) < 1)
			return;

		// Not on top
		if (entityIn.getY() - .25f < pos.getY())
			return;

		// Lock entities in place
		boolean isPlayer = entityIn instanceof PlayerEntity;
		if (entityIn instanceof LivingEntity && !isPlayer) 
			((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 10, 1, false, false));

		final Direction beltFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		final BeltSlope slope = blockState.getValue(BeltBlock.SLOPE);
		final Axis axis = beltFacing.getAxis();
		float movementSpeed = beltTe.getBeltMovementSpeed();
		final Direction movementDirection = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);

		Vector3i centeringDirection = Direction.get(POSITIVE, beltFacing.getClockWise()
			.getAxis())
			.getNormal();
		Vector3d movement = Vector3d.atLowerCornerOf(movementDirection.getNormal())
			.scale(movementSpeed);

		double diffCenter =
			axis == Axis.Z ? (pos.getX() + .5f - entityIn.getX()) : (pos.getZ() + .5f - entityIn.getZ());
		if (Math.abs(diffCenter) > 48 / 64f)
			return;

		BeltPart part = blockState.getValue(BeltBlock.PART);
		float top = 13 / 16f;
		boolean onSlope = notHorizontal && (part == BeltPart.MIDDLE || part == BeltPart.PULLEY
			|| part == (slope == BeltSlope.UPWARD ? BeltPart.END : BeltPart.START) && entityIn.getY() - pos.getY() < top
			|| part == (slope == BeltSlope.UPWARD ? BeltPart.START : BeltPart.END)
				&& entityIn.getY() - pos.getY() > top);

		boolean movingDown = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (beltFacing.getAxis() == Axis.Z) {
			boolean b = movingDown;
			movingDown = movingUp;
			movingUp = b;
		}

		if (movingUp)
			movement = movement.add(0, Math.abs(axis.choose(movement.x, movement.y, movement.z)), 0);
		if (movingDown)
			movement = movement.add(0, -Math.abs(axis.choose(movement.x, movement.y, movement.z)), 0);

		Vector3d centering = Vector3d.atLowerCornerOf(centeringDirection).scale(diffCenter * Math.min(Math.abs(movementSpeed), .1f) * 4);

		if (!(entityIn instanceof LivingEntity)
			|| ((LivingEntity) entityIn).zza == 0 && ((LivingEntity) entityIn).xxa == 0)
			movement = movement.add(centering);
		
		float step = entityIn.maxUpStep;
		if (!isPlayer) 
			entityIn.maxUpStep = 1;

		// Entity Collisions
		if (Math.abs(movementSpeed) < .5f) {
			Vector3d checkDistance = movement.normalize()
				.scale(0.5);
			AxisAlignedBB bb = entityIn.getBoundingBox();
			AxisAlignedBB checkBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
			checkBB = checkBB.move(checkDistance)
				.inflate(-Math.abs(checkDistance.x), -Math.abs(checkDistance.y), -Math.abs(checkDistance.z));
			List<Entity> list = world.getEntities(entityIn, checkBB);
			list.removeIf(e -> shouldIgnoreBlocking(entityIn, e));
			if (!list.isEmpty()) {
				entityIn.setDeltaMovement(0, 0, 0);
				info.ticksSinceLastCollision--;
				return;
			}
		}

		entityIn.fallDistance = 0;

		if (movingUp) {
			float minVelocity = .13f;
			float yMovement = (float) -(Math.max(Math.abs(movement.y), minVelocity));
			entityIn.move(SELF, new Vector3d(0, yMovement, 0));
			entityIn.move(SELF, movement.multiply(1, 0, 1));
		} else if (movingDown) {
			entityIn.move(SELF, movement.multiply(1, 0, 1));
			entityIn.move(SELF, movement.multiply(0, 1, 0));
		} else {
			entityIn.move(SELF, movement);
		}
		
		entityIn.onGround = true;

		if (!isPlayer)
			entityIn.maxUpStep = step;

		boolean movedPastEndingSlope = onSlope && (AllBlocks.BELT.has(world.getBlockState(entityIn.blockPosition()))
			|| AllBlocks.BELT.has(world.getBlockState(entityIn.blockPosition()
				.below())));

		if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
			entityIn.setPos(entityIn.getX(), entityIn.getY() + movement.y, entityIn.getZ());
		if (movedPastEndingSlope) {
			entityIn.setDeltaMovement(movement);
			entityIn.hurtMarked = true;
		}
		
	}

	public static boolean shouldIgnoreBlocking(Entity me, Entity other) {
		if (other instanceof AbstractContraptionEntity)
			return true;
		if (other instanceof HangingEntity)
			return true;
		return isRidingOrBeingRiddenBy(me, other);
	}

	public static boolean isRidingOrBeingRiddenBy(Entity me, Entity other) {
		for (Entity entity : me.getPassengers()) {
			if (entity.equals(other))
				return true;
			if (isRidingOrBeingRiddenBy(entity, other))
				return true;
		}
		return false;
	}

}
