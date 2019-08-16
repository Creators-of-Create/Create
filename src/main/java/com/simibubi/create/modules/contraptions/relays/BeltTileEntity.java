package com.simibubi.create.modules.contraptions.relays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BeltTileEntity extends KineticTileEntity implements ITickableTileEntity {

	protected BlockPos controller;
	public Map<Entity, TransportedEntityInfo> passengers;

	protected static class TransportedEntityInfo {
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
	}

	public BeltTileEntity() {
		super(AllTileEntities.BELT.type);
		controller = BlockPos.ZERO;
		passengers = new HashMap<>();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Controller", NBTUtil.writeBlockPos(controller));
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		super.read(compound);
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller;
	}

	public boolean isController() {
		return controller.equals(pos);
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.typeOf(getBlockState()))
			return false;
		return getBlockState().get(BeltBlock.PART) == Part.END || getBlockState().get(BeltBlock.PART) == Part.START;
	}

	@Override
	public void tick() {
		if (!isController())
			return;

		passengers.forEach((entity, info) -> {
			transportEntity(entity, info);
		});

		List<Entity> toRemove = new ArrayList<>();
		passengers.forEach((entity, info) -> {
			if (!canTransport(entity))
				toRemove.add(entity);
			if (info.ticksSinceLastCollision > 0) {
				toRemove.add(entity);
			}
			info.tick();
		});
		toRemove.forEach(passengers::remove);

		if (speed == 0)
			return;
	}

	public void transportEntity(Entity entityIn, TransportedEntityInfo info) {
		BlockPos pos = info.lastCollidedPos;
		TileEntity te = world.getTileEntity(pos);
		TileEntity tileEntityBelowPassenger = world.getTileEntity(entityIn.getPosition());
		BlockState blockState = info.lastCollidedState;

		boolean onEndingBelt = blockState.getBlock() instanceof BeltBlock && BeltBlock.isUpperEnd(blockState, speed);
		Direction movementFacing = Direction.getFacingFromAxisDirection(
				blockState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
				speed < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);

		boolean hasBeltAdjacent = onEndingBelt
				&& AllBlocks.BELT.typeOf(world.getBlockState(pos.offset(movementFacing)));
		boolean collidedWithBelt = te instanceof BeltTileEntity;
		boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

		// Don't fight other Belts
		if (!collidedWithBelt || betweenBelts) {
			return;
		}

		if (((KineticTileEntity) te).getSpeed() == 0)
			return;

		if (entityIn.posY - .25f < pos.getY())
			return;

		if (entityIn instanceof LivingEntity) {
			((LivingEntity) entityIn).setIdleTime(20);
		}

		final Direction beltFacing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
		final Slope slope = blockState.get(BeltBlock.SLOPE);
		final Axis axis = beltFacing.getAxis();
		float movementSpeed = ((KineticTileEntity) te).getSpeed() / 1600f;
		final Direction movementDirection = Direction
				.getFacingFromAxis(axis == Axis.X ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, axis);

		Vec3i centeringDirection = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis == Axis.X ? Axis.Z : Axis.X)
				.getDirectionVec();
		Vec3d movement = new Vec3d(movementDirection.getDirectionVec()).scale(movementSpeed);

		double diffCenter = axis == Axis.Z ? (pos.getX() + .5f - entityIn.posX) : (pos.getZ() + .5f - entityIn.posZ);
		if (Math.abs(diffCenter) > 48 / 64f)
			return;

		Part part = blockState.get(BeltBlock.PART);
		float top = 13 / 16f;
		boolean onSlope = part == Part.MIDDLE
				|| part == (slope == Slope.UPWARD ? Part.END : Part.START) && entityIn.posY - pos.getY() < top
				|| part == (slope == Slope.UPWARD ? Part.START : Part.END) && entityIn.posY - pos.getY() > top;

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

		if (info.ticksSinceLastCollision > 0 && !betweenBelts && onEndingBelt && !hasBeltAdjacent) {
			entityIn.setPosition(entityIn.posX, entityIn.posY + movement.y, entityIn.posZ);
			float verticalMultiplier = entityIn instanceof ItemEntity ? .25f : 1;
			if (movementSpeed > .25f)
				movement = movement.add(0, Math.abs(movementSpeed) * verticalMultiplier, 0);
			entityIn.setMotion(movement);
			return;
		}

		float step = entityIn.stepHeight;
		entityIn.stepHeight = 1;

		if (movingUp) {
			float minVelocity = entityIn instanceof ItemEntity ? .09f : .13f;
			float yMovement = (float) (Math.signum(movementSpeed) * Math.max(Math.abs(movement.y), minVelocity));
			entityIn.move(MoverType.SELF, new Vec3d(0, yMovement, 0));
			entityIn.move(MoverType.SELF, movement.mul(1, 0, 1));
		} else if (movingDown) {
			entityIn.move(MoverType.SELF, movement.mul(1, 0, 1));
			entityIn.move(MoverType.SELF, movement.mul(0, 1, 0));
		} else {
			entityIn.move(MoverType.SELF, movement);
		}
		entityIn.stepHeight = step;

		if (!betweenBelts && onEndingBelt && !hasBeltAdjacent) {
			entityIn.setMotion(movement);
		}
	}

	public boolean canTransport(Entity entity) {
		if (!entity.isAlive())
			return false;
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isSneaking())
			return false;

		return true;
	}

}
