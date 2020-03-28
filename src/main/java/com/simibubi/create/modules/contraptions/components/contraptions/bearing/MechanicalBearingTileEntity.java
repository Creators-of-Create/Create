package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.DirectionalExtenderScrollOptionSlot;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity implements IBearingTileEntity {

	protected boolean isWindmill;
	protected ContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;
	protected ScrollOptionBehaviour<RotationMode> movementMode;

	public MechanicalBearingTileEntity() {
		super(AllTileEntities.MECHANICAL_BEARING.type);
		isWindmill = false;
		setLazyTickRate(3);
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
	public float getAddedStressCapacity() {
		return isWindmill ? super.getAddedStressCapacity() : 0;
	}

	@Override
	public float getStressApplied() {
		return isWindmill ? 0 : super.getStressApplied();
	}

	public void neighbourChanged() {
		boolean shouldWindmill = world.isBlockPowered(pos);
		if (shouldWindmill == isWindmill)
			return;

		isWindmill = shouldWindmill;
		if (isWindmill && !running)
			assembleNextTick = true;
		if (isWindmill && running)
			updateGeneratedRotation();

		if (!isWindmill && running) {
			updateGeneratedRotation();
			if (getSpeed() == 0)
				assembleNextTick = true;
		}

		sendData();
	}

	@Override
	public void remove() {
		if (!world.isRemote)
			disassemble();
		super.remove();
	}

	@Override
	public float getGeneratedSpeed() {
		if (!running || !isWindmill)
			return 0;
		if (movedContraption == null)
			return 0;
		int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks() / 8;
		return MathHelper.clamp(sails, 1, 16);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putBoolean("Windmill", isWindmill);
		tag.putFloat("Angle", angle);
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		isWindmill = tag.getBoolean("Windmill");
		angle = tag.getFloat("Angle");
		super.read(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		float angleBefore = angle;
		super.readClientUpdate(tag);
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
		float speed = getSpeed() * 3 / 10f;
		if (world.isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	public void assemble() {
		Direction direction = getBlockState().get(FACING);

		// Collect Construct
		BearingContraption contraption = BearingContraption.assembleBearingAt(world, pos, direction);
		if (contraption == null)
			return;
		if (isWindmill && contraption.getSailBlocks() == 0)
			return;
		if (contraption.blocks.isEmpty())
			return;
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		movedContraption = ContraptionEntity.createStationary(world, contraption).controlledBy(this);
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
		if (!running)
			return;
		if (movedContraption != null)
			movedContraption.disassemble();

		movedContraption = null;
		running = false;
		angle = 0;
		updateGeneratedRotation();
		assembleNextTick = false;
		sendData();
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote)
			clientAngleDiff /= 2;

		if (running && Contraption.isFrozen())
			disassemble();

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE
						|| (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
				if (speed == 0 && (canDisassemble || movedContraption == null
						|| movedContraption.getContraption().blocks.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption().stop(world);
					disassemble();
				}
				return;
			} else {
				if (speed == 0 && !isWindmill)
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
			Axis axis = getBlockState().get(FACING).getAxis();
			Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
			Vec3d vec = new Vec3d(1, 1, 1).scale(angle).mul(new Vec3d(direction.getDirectionVec()));
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
		if (!world.isRemote)
			sendData();
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

	protected ValueBoxTransform getMovementModeSlot() {
		return new DirectionalExtenderScrollOptionSlot((state, d) -> {
			Axis axis = d.getAxis();
			Axis bearingAxis = state.get(MechanicalBearingBlock.FACING).getAxis();
			return bearingAxis != axis;
		});
	}

	@Override
	public void collided() {
	}

	@Override
	public boolean isAttachedTo(ContraptionEntity contraption) {
		return movedContraption == contraption;
	}

}
