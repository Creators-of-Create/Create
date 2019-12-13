package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.IControlContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalPistonTileEntity extends KineticTileEntity implements IControlContraption {

	protected float offset;
	protected boolean running;
	protected boolean assembleNextTick;
	protected boolean hadCollisionWithOtherPiston;

	protected ContraptionEntity movedContraption;
	protected int extensionLength;

	public MechanicalPistonTileEntity() {
		super(AllTileEntities.MECHANICAL_PISTON.type);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	@Override
	public void remove() {
		this.removed = true;
		if (!world.isRemote)
			disassembleConstruct();
		super.remove();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putFloat("Offset", offset);
		tag.putInt("ExtensionLength", extensionLength);
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		offset = tag.getFloat("Offset");
		extensionLength = tag.getInt("ExtensionLength");
		super.read(tag);
	}

	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		PistonContraption contraption = PistonContraption.movePistonAt(world, pos, direction, getMovementSpeed() < 0);
		if (contraption == null)
			return;

		// Check if not at limit already
		float resultingOffset = contraption.initialExtensionProgress + Math.signum(getMovementSpeed()) * .5f;
		extensionLength = contraption.extensionLength;
		if (resultingOffset <= 0 || resultingOffset >= extensionLength) {
			return;
		}

		// Run
		running = true;
		offset = contraption.initialExtensionProgress;
		sendData();

		BlockPos startPos = BlockPos.ZERO.offset(direction, contraption.initialExtensionProgress);
		contraption.removeBlocksFromWorld(world, startPos);
		movedContraption = new ContraptionEntity(getWorld(), contraption, 0).controlledBy(this);
		moveContraption();
		world.addEntity(movedContraption);
	}

	public void disassembleConstruct() {
		if (!running)
			return;

		if (!removed)
			getWorld().setBlockState(pos, getBlockState().with(MechanicalPistonBlock.STATE, PistonState.EXTENDED), 3);
		movedContraption.disassemble();
		running = false;
		movedContraption = null;
		sendData();

		if (removed)
			AllBlocks.MECHANICAL_PISTON.get().onBlockHarvested(world, pos, getBlockState(), null);
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (getSpeed() == 0)
					disassembleConstruct();
				else
					sendData();
				return;
			}
			assembleConstruct();
			return;
		}

		if (!running)
			return;

		float movementSpeed = getMovementSpeed();
		float newOffset = offset + movementSpeed;

		if (movedContraption == null)
			return;
		if (!world.isRemote && getModulatedOffset(newOffset) != getModulatedOffset(offset)) {
			offset = newOffset;
			sendData();
		}

		offset = newOffset;
		moveContraption();

		if (offset <= 0 || offset >= extensionLength) {
			offset = offset <= 0 ? 0 : extensionLength;
			if (!world.isRemote)
				disassembleConstruct();
			return;
		}
	}

	public void moveContraption() {
		if (movedContraption != null) {
			Vec3d constructOffset = getConstructOffset(0.5f);
			Vec3d vec = constructOffset.add(new Vec3d(movedContraption.getContraption().getAnchor()));
			movedContraption.setPosition(vec.x, vec.y, vec.z);
		}
	}

//	private boolean hasBlockCollisions(float newOffset) {
//		if (PistonContraption.isFrozen())
//			return true;
//
//		Direction movementDirection = getBlockState().get(BlockStateProperties.FACING);
//		BlockPos relativePos = BlockPos.ZERO.offset(movementDirection, getModulatedOffset(newOffset));
//
//		// Other moving Pistons
//		int maxPossibleRange = parameters.maxPistonPoles.get() + parameters.maxChassisRange.get()
//				+ parameters.maxChassisForTranslation.get();
//		Iterator<MechanicalPistonTileEntity> iterator = Create.constructHandler.getOtherMovingPistonsInWorld(this)
//				.iterator();
//		pistonLoop: while (iterator.hasNext()) {
//			MechanicalPistonTileEntity otherPiston = iterator.next();
//
//			if (otherPiston == this)
//				continue;
//			if (!otherPiston.running || otherPiston.movedContraption == null) {
//				iterator.remove();
//				continue;
//			}
//			if (otherPiston.pos.manhattanDistance(pos) > maxPossibleRange * 2)
//				continue;
//
//			Direction otherMovementDirection = otherPiston.getBlockState().get(BlockStateProperties.FACING);
//			BlockPos otherRelativePos = BlockPos.ZERO.offset(otherMovementDirection,
//					getModulatedOffset(otherPiston.offset));
//
//			for (AxisAlignedBB tBB : Arrays.asList(movedContraption.constructCollisionBox,
//					movedContraption.pistonCollisionBox)) {
//				for (AxisAlignedBB oBB : Arrays.asList(otherPiston.movedContraption.constructCollisionBox,
//						otherPiston.movedContraption.pistonCollisionBox)) {
//					if (tBB == null || oBB == null)
//						continue;
//
//					boolean frontalCollision = otherMovementDirection == movementDirection.getOpposite();
//					BlockPos thisColliderOffset = relativePos.offset(movementDirection,
//							frontalCollision ? (getMovementSpeed() > 0 ? 1 : -1) : 0);
//					AxisAlignedBB thisBB = tBB.offset(thisColliderOffset);
//					AxisAlignedBB otherBB = oBB.offset(otherRelativePos);
//
//					if (thisBB.intersects(otherBB)) {
//						boolean actuallyColliding = false;
//						for (BlockPos colliderPos : movedContraption.getColliders(world, movementDirection)) {
//							colliderPos = colliderPos.add(thisColliderOffset).subtract(otherRelativePos);
//							if (!otherPiston.movedContraption.blocks.containsKey(colliderPos))
//								continue;
//							actuallyColliding = true;
//						}
//						if (!actuallyColliding)
//							continue pistonLoop;
//						hadCollisionWithOtherPiston = true;
//						return true;
//					}
//
//				}
//			}
//
//		}
//
//		if (!running)
//			return false;
//
//		// Other Blocks in world
//		for (BlockPos pos : movedContraption.getColliders(world,
//				getMovementSpeed() > 0 ? movementDirection : movementDirection.getOpposite())) {
//			BlockPos colliderPos = pos.add(relativePos);
//
//			if (!world.isBlockPresent(colliderPos))
//				return true;
//			if (!world.getBlockState(colliderPos).getMaterial().isReplaceable()
//					&& !world.getBlockState(colliderPos).getCollisionShape(world, colliderPos).isEmpty())
//				return true;
//		}
//
//		return false;
//	}

	private int getModulatedOffset(float offset) {
		return MathHelper.clamp((int) (offset + .5f), 0, extensionLength);
	}

	public float getMovementSpeed() {
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		int movementModifier = pistonDirection.getAxisDirection().getOffset()
				* (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
		return getSpeed() * -movementModifier / 1024f;
	}

	public Vec3d getConstructOffset(float partialTicks) {
		float interpolatedOffset = MathHelper.clamp(offset + (partialTicks - .5f) * getMovementSpeed(), 0,
				extensionLength);
		return new Vec3d(getBlockState().get(BlockStateProperties.FACING).getDirectionVec()).scale(interpolatedOffset);
	}

	@Override
	public void attach(ContraptionEntity contraption) {
		if (contraption.getContraption() instanceof PistonContraption) {
			this.movedContraption = contraption;
			if (!world.isRemote)
				sendData();
		}
	}

}
