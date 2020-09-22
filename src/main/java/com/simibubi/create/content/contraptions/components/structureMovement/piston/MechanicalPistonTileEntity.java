package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.DirectionalExtenderScrollOptionSlot;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MechanicalPistonTileEntity extends LinearActuatorTileEntity {

	protected boolean hadCollisionWithOtherPiston;
	protected int extensionLength;

	public MechanicalPistonTileEntity(TileEntityType<? extends MechanicalPistonTileEntity> type) {
		super(type);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		extensionLength = compound.getInt("ExtensionLength");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	protected void write(CompoundNBT tag, boolean clientPacket) {
		tag.putInt("ExtensionLength", extensionLength);
		super.write(tag, clientPacket);
	}

	@Override
	public void assemble() {
		if (!(world.getBlockState(pos)
			.getBlock() instanceof MechanicalPistonBlock))
			return;

		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		PistonContraption contraption = PistonContraption.movePistonAt(world, pos, direction, getMovementSpeed() < 0);
		Direction positive = Direction.getFacingFromAxis(AxisDirection.POSITIVE, direction.getAxis());
		Direction movementDirection =
			getSpeed() > 0 ^ direction.getAxis() != Axis.Z ? positive : positive.getOpposite();

		if (contraption != null) {
			BlockPos anchor = contraption.getAnchor()
				.offset(direction, contraption.initialExtensionProgress);
			if (ContraptionCollider.isCollidingWithWorld(world, contraption, anchor.offset(movementDirection),
				movementDirection))
				contraption = null;
		}

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
		movedContraption = ContraptionEntity.createStationary(getWorld(), contraption)
			.controlledBy(this);
		applyContraptionPosition();
		forceMove = true;
		world.addEntity(movedContraption);
	}

	@Override
	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		if (!removed)
			getWorld().setBlockState(pos, getBlockState().with(MechanicalPistonBlock.STATE, PistonState.EXTENDED),
				3 | 16);
		if (movedContraption != null) {
			applyContraptionPosition();
			movedContraption.disassemble();
		}
		running = false;
		movedContraption = null;
		sendData();

		if (removed)
			AllBlocks.MECHANICAL_PISTON.get()
				.onBlockHarvested(world, pos, getBlockState(), null);
	}

	@Override
	public void collided() {
		super.collided();
		if (!running && getMovementSpeed() > 0)
			assembleNextTick = true;
	}

	@Override
	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f;
		if (world.isRemote)
			movementSpeed *= ServerSpeedProvider.get();
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		int movementModifier = pistonDirection.getAxisDirection()
			.getOffset() * (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
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
	protected void visitNewPosition() {}

	@Override
	protected Vector3d toMotionVector(float speed) {
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		return Vector3d.of(pistonDirection.getDirectionVec()).scale(speed);
	}

	@Override
	protected Vector3d toPosition(float offset) {
		Vector3d position = Vector3d.of(getBlockState().get(BlockStateProperties.FACING)
			.getDirectionVec()).scale(offset);
		return position.add(Vector3d.of(movedContraption.getContraption()
			.getAnchor()));
	}

	@Override
	protected ValueBoxTransform getMovementModeSlot() {
		return new DirectionalExtenderScrollOptionSlot((state, d) -> {
			Axis axis = d.getAxis();
			Axis extensionAxis = state.get(MechanicalPistonBlock.FACING)
				.getAxis();
			Axis shaftAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
			return extensionAxis != axis && shaftAxis != axis;
		});
	}

	@Override
	protected int getInitialOffset() {
		return movedContraption == null ? 0
			: ((PistonContraption) movedContraption.getContraption()).initialExtensionProgress;
	}

}
