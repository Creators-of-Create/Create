package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.DirectionalExtenderScrollOptionSlot;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalPistonTileEntity extends LinearActuatorTileEntity {

	protected boolean hadCollisionWithOtherPiston;
	protected int extensionLength;

	public MechanicalPistonTileEntity() {
		super(AllTileEntities.MECHANICAL_PISTON.type);
	}

	@Override
	public void read(CompoundNBT tag) {
		extensionLength = tag.getInt("ExtensionLength");
		super.read(tag);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putInt("ExtensionLength", extensionLength);
		return super.write(tag);
	}

	@Override
	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		PistonContraption contraption = PistonContraption.movePistonAt(world, pos, direction, getMovementSpeed() < 0);
		if (contraption == null)
			return;

		// Check if not at limit already
		extensionLength = contraption.extensionLength;
		float resultingOffset = contraption.initialExtensionProgress + Math.signum(getMovementSpeed()) * .5f;
		if (resultingOffset <= 0 || resultingOffset >= extensionLength) {
			return;
		}

		// Run
		running = true;
		offset = contraption.initialExtensionProgress;
		sendData();
		clientOffsetDiff = 0;

		BlockPos startPos = BlockPos.ZERO.offset(direction, contraption.initialExtensionProgress);
		contraption.removeBlocksFromWorld(world, startPos);
		movedContraption = ContraptionEntity.createStationary(getWorld(), contraption).controlledBy(this);
		applyContraptionPosition();
		forceMove = true;
		world.addEntity(movedContraption);
	}

	@Override
	public void disassembleConstruct() {
		if (!running)
			return;
		if (!removed)
			getWorld().setBlockState(pos, getBlockState().with(MechanicalPistonBlock.STATE, PistonState.EXTENDED), 3);
		if (movedContraption != null) {
			applyContraptionPosition();
			movedContraption.disassemble();
		}
		running = false;
		movedContraption = null;
		sendData();

		if (removed)
			AllBlocks.MECHANICAL_PISTON.get().onBlockHarvested(world, pos, getBlockState(), null);
	}

	@Override
	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f;
		if (world.isRemote)
			movementSpeed *= ServerSpeedProvider.get();
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		int movementModifier =
			pistonDirection.getAxisDirection().getOffset() * (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
		movementSpeed = movementSpeed * -movementModifier + clientOffsetDiff / 2f;

		int extensionRange = getExtensionRange();
		movementSpeed = MathHelper.clamp(movementSpeed, 0 - offset, extensionRange - offset);
		return movementSpeed;
	}

	@Override
	protected int getExtensionRange() {
		return extensionLength;
	}

	@Override
	protected void visitNewPosition() {
	}

	@Override
	protected Vec3d toMotionVector(float speed) {
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		return new Vec3d(pistonDirection.getDirectionVec()).scale(speed);
	}

	@Override
	protected Vec3d toPosition(float offset) {
		Vec3d position = new Vec3d(getBlockState().get(BlockStateProperties.FACING).getDirectionVec()).scale(offset);
		return position.add(new Vec3d(movedContraption.getContraption().getAnchor()));
	}

	@Override
	protected ValueBoxTransform getMovementModeSlot() {
		return new DirectionalExtenderScrollOptionSlot((state, d) -> {
			Axis axis = d.getAxis();
			Axis extensionAxis = state.get(MechanicalPistonBlock.FACING).getAxis();
			Axis shaftAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
			return extensionAxis != axis && shaftAxis != axis;
		});
	}

	@Override
	protected int getInitialOffset() {
		return movedContraption == null ? 0
				: ((PistonContraption) movedContraption.getContraption()).initialExtensionProgress;
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

}
