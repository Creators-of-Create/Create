package com.simibubi.create.modules.contraptions.relays.advanced;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.motor.MotorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class SpeedControllerTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour targetSpeed;

	public SpeedControllerTileEntity() {
		super(AllTileEntities.ROTATION_SPEED_CONTROLLER.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

		targetSpeed = new ScrollValueBehaviour(Lang.translate("generic.speed"), this,
				new ControllerValueBoxTransform());
		targetSpeed.between(-max, max);
		targetSpeed.value = DEFAULT_SPEED;
		targetSpeed.moveText(new Vec3d(9, 0, 10));
		targetSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		targetSpeed.withCallback(i -> this.updateTargetRotation());
		targetSpeed.withStepFunction(MotorTileEntity::step);
		behaviours.add(targetSpeed);
	}

	private void updateTargetRotation() {
		if (hasNetwork()) 
			getNetwork().remove(this);
		RotationPropagator.handleRemoved(world, pos, this);
		removeSource();
		attachKinetics();
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}
	
	public static float getSpeedModifier(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
			boolean targetingController) {
		if (!(speedControllerIn instanceof SpeedControllerTileEntity))
			return 1;
		SpeedControllerTileEntity speedController = (SpeedControllerTileEntity) speedControllerIn;
		float targetSpeed = speedController.targetSpeed.getValue();
		float speed = speedControllerIn.getSpeed();

		if (targetSpeed == 0)
			return 0;
		float wheelSpeed = cogWheel.getTheoreticalSpeed();
		if (targetingController && wheelSpeed == 0)
			return 1;

		if (!speedController.hasSource()) {
			if (targetingController)
				return targetSpeed / wheelSpeed;
			return 1;
		}

		boolean wheelPowersController = speedController.getSource().equals(cogWheel.getPos());
		
		if (wheelPowersController) {
			if (targetingController)
				return targetSpeed / wheelSpeed;
			return wheelSpeed / targetSpeed;
		}
		
		if (targetingController)
			return speed / targetSpeed;
		return targetSpeed / speed;
	}

	private class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

		@Override
		protected Vec3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 11.5f, 14);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			if (direction.getAxis().isVertical())
				return false;
			return state.get(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
		}

		@Override
		protected float getScale() {
			return 0.275f;
		}

	}

}
