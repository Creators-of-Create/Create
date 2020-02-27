package com.simibubi.create.modules.contraptions.components.motor;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.entity.player.PlayerEntity;

public class MotorTileEntity extends GeneratingKineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour generatedSpeed;

	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
		updateNetwork = true;
		newNetworkID = UUID.randomUUID();
		speed = DEFAULT_SPEED;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxMotorSpeed.get();

		CenteredSideValueBoxTransform slot = new CenteredSideValueBoxTransform((motor, side) -> {
			return motor.get(MotorBlock.HORIZONTAL_FACING) == side.getOpposite();
		});

		generatedSpeed = new ScrollValueBehaviour(Lang.translate("generic.speed"), this, slot);
		generatedSpeed.between(-max, max);
		generatedSpeed.value = DEFAULT_SPEED;
		generatedSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
		generatedSpeed.withStepFunction(this::step);
		behaviours.add(generatedSpeed);
	}

	@Override
	public float getGeneratedSpeed() {
		return generatedSpeed.getValue();
	}

	private int step(int current, boolean forward) {
		PlayerEntity closestPlayer = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ());
		if (closestPlayer != null && closestPlayer.isSneaking())
			return 1;

		int magnitude = Math.abs(current) - (forward == current > 0 ? 0 : 1);
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
