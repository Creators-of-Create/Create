package com.simibubi.create.modules.contraptions.components.motor;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

public class MotorTileEntity extends GeneratingKineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour generatedSpeed;

	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxMotorSpeed.get();

		CenteredSideValueBoxTransform slot =
			new CenteredSideValueBoxTransform((motor, side) -> motor.get(MotorBlock.FACING) == side.getOpposite());

		generatedSpeed = new ScrollValueBehaviour(Lang.translate("generic.speed"), this, slot);
		generatedSpeed.between(-max, max);
		generatedSpeed.value = DEFAULT_SPEED;
		generatedSpeed.scrollableValue = DEFAULT_SPEED;
		generatedSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
		generatedSpeed.withStepFunction(MotorTileEntity::step);
		behaviours.add(generatedSpeed);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		return generatedSpeed.getValue();
	}

	public static int step(StepContext context) {
		if (context.shift)
			return 1;

		int current = context.currentValue;
		int magnitude = Math.abs(current) - (context.forward == current > 0 ? 0 : 1);
		int step = 1;

		if (magnitude >= 4)
			step *= 4;
		if (magnitude >= 32)
			step *= 4;
		if (magnitude >= 128)
			step *= 4;
		return step;
	}

}
