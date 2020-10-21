package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.List;

import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity implements IBearingTileEntity {

	protected ScrollOptionBehaviour<RotationMode> movementMode;
	protected ContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;

	public MechanicalBearingTileEntity(TileEntityType<? extends MechanicalBearingTileEntity> type) {
		super(type);
		setLazyTickRate(3);
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(RotationMode.class, Lang.translate("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		behaviours.add(movementMode);
	}

	@Override
	public void remove() {
		if (!world.isRemote)
			disassemble();
		super.remove();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("Angle", angle);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		float angleBefore = angle;
		running = compound.getBoolean("Running");
		angle = compound.getFloat("Angle");
		super.read(compound, clientPacket);
		if (!clientPacket)
			return;
		if (running) {
			clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
			angle = angleBefore;
		} else
			movedContraption = null;
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (movedContraption == null || movedContraption.isStalled() || !running)
			partialTicks = 0;
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	public float getAngularSpeed() {
		float speed = (isWindmill() ? getGeneratedSpeed() : getSpeed()) * 3 / 10f;
		if (world.isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	protected boolean isWindmill() {
		return false;
	}

	public void assemble() {
		if (!(world.getBlockState(pos)
			.getBlock() instanceof BearingBlock))
			return;

		Direction direction = getBlockState().get(FACING);

		// Collect Construct
		BearingContraption contraption = BearingContraption.assembleBearingAt(world, pos, direction);
		if (contraption == null)
			return;
		if (isWindmill() && contraption.getSailBlocks() == 0)
			return;
		if (contraption.blocks.isEmpty())
			return;
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);

		movedContraption = ContraptionEntity.createStationary(world, contraption)
			.controlledBy(this);
		BlockPos anchor = pos.offset(direction);
		movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
		world.addEntity(movedContraption);

		// Run
		running = true;
		angle = 0;
		sendData();
		updateGeneratedRotation();
	}

	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		angle = 0;
		if (isWindmill())
			applyRotation();
		if (movedContraption != null)
			movedContraption.disassemble();

		movedContraption = null;
		running = false;
		updateGeneratedRotation();
		assembleNextTick = false;
		sendData();
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote)
			clientAngleDiff /= 2;

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE
					|| (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
				if (speed == 0 && (canDisassemble || movedContraption == null
					|| movedContraption.getContraption().blocks.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption()
							.stop(world);
					disassemble();
				}
				return;
			} else {
				if (speed == 0 && !isWindmill())
					return;
				assemble();
			}
			return;
		}

		if (!running)
			return;

		if (!(movedContraption != null && movedContraption.isStalled())) {
			float angularSpeed = getAngularSpeed();
			float newAngle = angle + angularSpeed;
			angle = (float) (newAngle % 360);
		}

		applyRotation();
	}

	public boolean isNearInitialAngle() {
		return Math.abs(angle) < 45 || Math.abs(angle) > 7 * 45;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !world.isRemote)
			sendData();
	}

	protected void applyRotation() {
		if (movedContraption != null) {
			Axis axis = getBlockState().get(FACING)
				.getAxis();
			Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
			Vec3d vec = new Vec3d(1, 1, 1).scale(angle)
				.mul(new Vec3d(direction.getDirectionVec()));
			movedContraption.rotateTo(vec.x, vec.y, vec.z);
		}
	}

	@Override
	public void attach(ContraptionEntity contraption) {
		BlockState blockState = getBlockState();
		if (!(contraption.getContraption() instanceof BearingContraption))
			return;
		if (!blockState.has(FACING))
			return;

		this.movedContraption = contraption;
		markDirty();
		BlockPos anchor = pos.offset(blockState.get(FACING));
		movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
		if (!world.isRemote) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void onStall() {
		if (!world.isRemote)
			sendData();
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public void collided() {}

	@Override
	public boolean isAttachedTo(ContraptionEntity contraption) {
		return movedContraption == contraption;
	}

	public boolean isRunning() {
		return running;
	}

}
