package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.List;

import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SpeedControllerTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour targetSpeed;

	boolean hasBracket;

	public SpeedControllerTileEntity(TileEntityType<? extends SpeedControllerTileEntity> type) {
		super(type);
		hasBracket = false;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBracket();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

		targetSpeed =
			new ScrollValueBehaviour(Lang.translate("generic.speed"), this, new ControllerValueBoxTransform());
		targetSpeed.between(-max, max);
		targetSpeed.value = DEFAULT_SPEED;
		targetSpeed.moveText(new Vector3d(9, 0, 10));
		targetSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		targetSpeed.withCallback(i -> this.updateTargetRotation());
		targetSpeed.withStepFunction(CreativeMotorTileEntity::step);
		behaviours.add(targetSpeed);
	}

	private void updateTargetRotation() {
		if (hasNetwork())
			getOrCreateNetwork().remove(this);
		RotationPropagator.handleRemoved(level, worldPosition, this);
		removeSource();
		attachKinetics();
	}

	public static float getConveyedSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
		boolean targetingController) {
		if (!(speedControllerIn instanceof SpeedControllerTileEntity))
			return 0;

		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();
		float desiredOutputSpeed = getDesiredOutputSpeed(cogWheel, speedControllerIn, targetingController);

		float compareSpeed = targetingController ? speed : wheelSpeed;
		if (desiredOutputSpeed >= 0 && compareSpeed >= 0)
			return Math.max(desiredOutputSpeed, compareSpeed);
		if (desiredOutputSpeed < 0 && compareSpeed < 0)
			return Math.min(desiredOutputSpeed, compareSpeed);

		return desiredOutputSpeed;
	}

	public static float getDesiredOutputSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
		boolean targetingController) {
		SpeedControllerTileEntity speedController = (SpeedControllerTileEntity) speedControllerIn;
		float targetSpeed = speedController.targetSpeed.getValue();
		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();

		if (targetSpeed == 0)
			return 0;
		if (targetingController && wheelSpeed == 0)
			return 0;
		if (!speedController.hasSource()) {
			if (targetingController)
				return targetSpeed;
			return 0;
		}

		boolean wheelPowersController = speedController.source.equals(cogWheel.getBlockPos());

		if (wheelPowersController) {
			if (targetingController)
				return targetSpeed;
			return wheelSpeed;
		}

		if (targetingController)
			return speed;
		return targetSpeed;
	}

	public void updateBracket() {
		if (level == null || !level.isClientSide)
			return;
		BlockState stateAbove = level.getBlockState(worldPosition.above());
		hasBracket = ICogWheel.isDedicatedCogWheel(stateAbove.getBlock()) && ICogWheel.isLargeCog(stateAbove)
			&& stateAbove.getValue(CogWheelBlock.AXIS).isHorizontal();
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	private class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

		@Override
		protected Vector3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 11f, 16);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			if (direction.getAxis()
				.isVertical())
				return false;
			return state.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
		}

		@Override
		protected float getScale() {
			return 0.275f;
		}

	}

}
