package com.simibubi.create.content.optics.behaviour;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractRotatedLightRelayBehaviour<T extends KineticTileEntity & ILightHandler.ILightHandlerProvider & RotationMode.RotationModeProvider> extends AbstractLightRelayBehaviour<T> {
	protected float angle;
	protected float clientAngleDiff;
	private float prevAngle;

	protected AbstractRotatedLightRelayBehaviour(T te) {
		super(te);
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		nbt.putFloat("Angle", angle);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		angle = nbt.getFloat("Angle");
		super.read(nbt, clientPacket);
	}

	public float getInterpolatedAngle(float partialTicks) {
		if (tileEntity.isVirtual())
			return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
		if (handler.getMode() == RotationMode.ROTATE_LIMITED && Math.abs(angle) == 90)
			return angle;
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	public float getAngularSpeed() {
		float speed = handler.getSpeed() * 3 / 10f;
		if (handler.getSpeed() == 0)
			speed = 0;
		if (getHandlerWorld() != null && getHandlerWorld().isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	@Override
	public void tick() {
		super.tick();

		prevAngle = angle;
		if (getHandlerWorld() != null && getHandlerWorld().isRemote)
			clientAngleDiff /= 2;

		float angularSpeed = getAngularSpeed();
		float newAngle = angle + angularSpeed;
		angle = newAngle % 360;

		if (handler.getMode() == RotationMode.ROTATE_LIMITED)
			angle = MathHelper.clamp(angle, -90, 90);
		if (handler.getMode() == RotationMode.ROTATE_45 && angle == prevAngle) // don't snap while still rotating
			angle = 45F * Math.round(Math.round(angle) / 45F);


		if (angle != prevAngle) {
			onAngleChanged();
		}
	}

	protected void onAngleChanged() {
		updateBeams();
	}

	protected Direction.Axis getAxis() {
		return tileEntity.getBlockState()
				.get(BlockStateProperties.AXIS);
	}
}
