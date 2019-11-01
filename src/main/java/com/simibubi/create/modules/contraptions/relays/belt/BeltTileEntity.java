package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.Tracker;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BeltTileEntity extends KineticTileEntity {

	protected BlockPos controller;
	public Map<Entity, TransportedEntityInfo> passengers;
	public AllBeltAttachments.Tracker attachmentTracker;
	private CompoundNBT trackerUpdateTag;
	public int color;

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
		attachmentTracker = new Tracker(this);
		color = -1;
	}

	protected boolean isLastBelt() {
		if (speed == 0)
			return false;

		Direction direction = getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
		if (getBlockState().get(BeltBlock.SLOPE) == Slope.VERTICAL)
			return false;

		Part part = getBlockState().get(BeltBlock.PART);
		if (part == Part.MIDDLE)
			return false;

		boolean movingPositively = (speed > 0 == (direction.getAxisDirection().getOffset() == 1))
				^ direction.getAxis() == Axis.X;
		return part == Part.START ^ movingPositively;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Controller", NBTUtil.writeBlockPos(controller));
		compound.putInt("Color", color);
		attachmentTracker.write(compound);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		trackerUpdateTag = compound;
		color = compound.getInt("Color");
		super.read(compound);
	}

	public void applyColor(DyeColor colorIn) {
		int colorValue = colorIn.getMapColor().colorValue;
		for (BlockPos blockPos : BeltBlock.getBeltChain(world, getController())) {
			BeltTileEntity tileEntity = (BeltTileEntity) world.getTileEntity(blockPos);
			if (tileEntity != null) {
				if (tileEntity.color == -1) {
					tileEntity.color = colorValue;
				} else {
					tileEntity.color = ColorHelper.mixColors(tileEntity.color, colorValue, .5f);
				}
				tileEntity.sendData();
			}
		}
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
		super.tick();
		
		if (world != null && trackerUpdateTag != null) {
			attachmentTracker.readAndSearch(trackerUpdateTag, this);
			trackerUpdateTag = null;
		}
		if (!isController())
			return;
		if (passengers == null)
			passengers = new HashMap<>();

		passengers.forEach((entity, info) -> {
			transportEntity(entity, info);
		});

		List<Entity> toRemove = new ArrayList<>();
		passengers.forEach((entity, info) -> {
			if (!canTransport(entity))
				toRemove.add(entity);
			if (info.ticksSinceLastCollision > ((getBlockState().get(BeltBlock.SLOPE) != Slope.HORIZONTAL) ? 3 : 1)) {
				toRemove.add(entity);
			}
			info.tick();
		});
		toRemove.forEach(e -> {
			if (e instanceof ItemEntity)
				((ItemEntity) e).setAgeToCreativeDespawnTime();
			passengers.remove(e);
		});

		if (speed == 0)
			return;
	}

	public void transportEntity(Entity entityIn, TransportedEntityInfo info) {
		BlockPos pos = info.lastCollidedPos;
		TileEntity te = world.getTileEntity(pos);
		TileEntity tileEntityBelowPassenger = world.getTileEntity(entityIn.getPosition());
		BlockState blockState = info.lastCollidedState;
		Direction movementFacing = Direction.getFacingFromAxisDirection(
				blockState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
				speed < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);

		boolean collidedWithBelt = te instanceof BeltTileEntity;
		boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

		// Don't fight other Belts
		if (!collidedWithBelt || betweenBelts) {
			return;
		}

		// Too slow
		boolean notHorizontal = getBlockState().get(BeltBlock.SLOPE) != Slope.HORIZONTAL;
		if (Math.abs(getSpeed()) < (notHorizontal ? 32 : 1))
			return;

		// Not on top
		if (entityIn.posY - .25f < pos.getY())
			return;

		// Lock entities in place
		if (entityIn instanceof LivingEntity && !(entityIn instanceof PlayerEntity)) {
			((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 1, 9, false, false));
		}

		BeltTileEntity belt = (BeltTileEntity) te;

		// Attachment pauses movement
		for (BeltAttachmentState state : belt.attachmentTracker.attachments) {
			if (state.attachment.handleEntity(belt, entityIn, state)) {
				info.ticksSinceLastCollision--;
				return;
			}
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
		float maxDiffCenter = (entityIn instanceof ItemEntity) ? 32 / 64f : 48 / 64f;
		if (Math.abs(diffCenter) > maxDiffCenter)
			return;

		Part part = blockState.get(BeltBlock.PART);
		float top = 13 / 16f;
		boolean onSlope = notHorizontal && (part == Part.MIDDLE
				|| part == (slope == Slope.UPWARD ? Part.END : Part.START) && entityIn.posY - pos.getY() < top
				|| part == (slope == Slope.UPWARD ? Part.START : Part.END) && entityIn.posY - pos.getY() > top);

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
		if (!(entityIn instanceof PlayerEntity))
			entityIn.stepHeight = 1;

		// Entity Collisions
		if (Math.abs(movementSpeed) < .5f) {
			Vec3d checkDistance = movement.scale(2f).add(movement.normalize());
			AxisAlignedBB bb = entityIn.getBoundingBox();
			AxisAlignedBB checkBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
			if (!world
					.getEntitiesWithinAABBExcludingEntity(entityIn, checkBB.offset(checkDistance)
							.grow(-Math.abs(checkDistance.x), -Math.abs(checkDistance.y), -Math.abs(checkDistance.z)))
					.isEmpty()) {
				entityIn.setMotion(0, 0, 0);
				info.ticksSinceLastCollision--;
				return;
			}
		}

		if (movingUp) {
			float minVelocity = entityIn instanceof ItemEntity ? .09f : .13f;
			float yMovement = (float) -(Math.max(Math.abs(movement.y), minVelocity));
			entityIn.move(MoverType.SELF, new Vec3d(0, yMovement, 0));
			entityIn.move(MoverType.SELF, movement.mul(1, 0, 1));
		} else if (movingDown) {
			entityIn.move(MoverType.SELF, movement.mul(1, 0, 1));
			entityIn.move(MoverType.SELF, movement.mul(0, 1, 0));
		} else {
			entityIn.move(MoverType.SELF, movement);
		}

		if (!(entityIn instanceof PlayerEntity))
			entityIn.stepHeight = step;

		boolean movedPastEndingSlope = onSlope && (AllBlocks.BELT.typeOf(world.getBlockState(entityIn.getPosition()))
				|| AllBlocks.BELT.typeOf(world.getBlockState(entityIn.getPosition().down())));

		if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
			entityIn.setPosition(entityIn.posX, entityIn.posY + movement.y, entityIn.posZ);
		if (movedPastEndingSlope)
			entityIn.setMotion(movement);
	}

	public boolean canTransport(Entity entity) {
		if (!entity.isAlive())
			return false;
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isSneaking())
			return false;

		return true;
	}

}
