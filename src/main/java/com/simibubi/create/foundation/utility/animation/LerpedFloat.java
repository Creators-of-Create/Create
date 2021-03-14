package com.simibubi.create.foundation.utility.animation;

import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

// Can replace all Interpolated value classes
// InterpolatedChasingValue, InterpolatedValue, InterpolatedChasingAngle, InterpolatedAngle
public class LerpedFloat {

	Interpolater interpolater;
	float previousValue;
	float value;

	Chaser chaseFunction;
	float chaseTarget;
	float chaseSpeed;

	boolean forcedSync;

	public LerpedFloat(Interpolater interpolater) {
		this.interpolater = interpolater;
		startWithValue(0);
		forcedSync = true;
	}

	public static LerpedFloat linear() {
		return new LerpedFloat((p, c, t) -> (float) MathHelper.lerp(p, c, t));
	}

	public static LerpedFloat angular() {
		return new LerpedFloat(AngleHelper::angleLerp);
	}

	public LerpedFloat startWithValue(double value) {
		float f = (float) value;
		this.previousValue = f;
		this.chaseTarget = f;
		this.value = f;
		return this;
	}

	public LerpedFloat chase(double value, double speed, Chaser chaseFunction) {
		this.chaseTarget = (float) value;
		this.chaseSpeed = (float) speed;
		this.chaseFunction = chaseFunction;
		return this;
	}

	public void updateChaseTarget(float target) {
		this.chaseTarget = target;
	}

	public boolean updateChaseSpeed(double speed) {
		float prevSpeed = this.chaseSpeed;
		this.chaseSpeed = (float) speed;
		return !MathHelper.epsilonEquals(prevSpeed, speed);
	}

	public void tickChaser() {
		previousValue = value;
		if (chaseFunction == null)
			return;
		if (MathHelper.epsilonEquals((double) value, chaseTarget)) {
			value = chaseTarget;
			return;
		}
		value = chaseFunction.chase(value, chaseSpeed, chaseTarget);
	}

	public void setValue(double value) {
		this.previousValue = this.value;
		this.value = (float) value;
	}

	public float getValue() {
		return getValue(1);
	}

	public float getValue(float partialTicks) {
		return MathHelper.lerp(partialTicks, previousValue, value);
	}

	public float getChaseTarget() {
		return chaseTarget;
	}

	public void forceNextSync() {
		forcedSync = true;
	}

	public CompoundNBT writeNBT() {
		CompoundNBT compoundNBT = new CompoundNBT();
		compoundNBT.putFloat("Speed", chaseSpeed);
		compoundNBT.putFloat("Target", chaseTarget);
		compoundNBT.putFloat("Value", value);
		if (forcedSync)
			compoundNBT.putBoolean("Force", true);
		forcedSync = false;
		return compoundNBT;
	}

	public void readNBT(CompoundNBT compoundNBT, boolean clientPacket) {
		if (!clientPacket || compoundNBT.contains("Force"))
			startWithValue(compoundNBT.getFloat("Value"));
		readChaser(compoundNBT);
	}

	private void readChaser(CompoundNBT compoundNBT) {
		chaseSpeed = compoundNBT.getFloat("Speed");
		chaseTarget = compoundNBT.getFloat("Target");
	}

	@FunctionalInterface
	public interface Interpolater {
		float interpolate(double progress, double current, double target);
	}

	@FunctionalInterface
	public interface Chaser {

		public static final Chaser IDLE = (c, s, t) -> (float) c;
		public static final Chaser EXP = exp(Double.MAX_VALUE);
		public static final Chaser LINEAR = (c, s, t) -> (float) (c + MathHelper.clamp(t - c, -s, s));

		public static Chaser exp(double maxEffectiveSpeed) {
			return (c, s, t) -> (float) (c + MathHelper.clamp((t - c) * s, -maxEffectiveSpeed, maxEffectiveSpeed));
		}

		float chase(double current, double speed, double target);
	}

}
