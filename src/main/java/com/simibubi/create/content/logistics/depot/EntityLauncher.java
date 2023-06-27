package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityLauncher {

	private int horizontalDistance;
	private int verticalDistance;
	private double yMotion;
	private double xMotion;
	private double totalFlyingTicks;

	public EntityLauncher(int horizontalDistance, int verticalDistance) {
		set(horizontalDistance, verticalDistance);
	}

	public void clamp(int max) {
		set(Math.min(horizontalDistance, max),
			Mth.sign(verticalDistance) * Math.min(Math.abs(verticalDistance), max));
	}

	public void set(int horizontalDistance, int verticalDistance) {
		this.horizontalDistance = horizontalDistance;
		this.verticalDistance = verticalDistance;
		recalculateTrajectory();
	}

	public void applyMotion(Entity entity, Direction facing) {
		Vec3 motionVec = new Vec3(0, yMotion, xMotion);
		motionVec = VecHelper.rotate(motionVec, AngleHelper.horizontalAngle(facing), Axis.Y);
		entity.setDeltaMovement(motionVec.x * .91, motionVec.y * .98, motionVec.z * .91);
	}

	public int getHorizontalDistance() {
		return horizontalDistance;
	}

	public int getVerticalDistance() {
		return verticalDistance;
	}

	public double getTotalFlyingTicks() {
		return totalFlyingTicks;
	}

	public Vec3 getGlobalPos(double t, Direction d, BlockPos launcher) {
		Vec3 start = new Vec3(launcher.getX() + .5f, launcher.getY() + .5f, launcher.getZ() + .5f);

		float xt = x(t);
		float yt = y(t);
		double progress = Mth.clamp(t / getTotalFlyingTicks(), 0, 1);
		double correctionStrength = Math.pow(progress, 3);

		Vec3 vec = new Vec3(0, yt + (verticalDistance - yt) * correctionStrength * 0.5f,
			xt + (horizontalDistance - xt) * correctionStrength);
		return VecHelper.rotate(vec, 180 + AngleHelper.horizontalAngle(d), Axis.Y)
			.add(start);
	}

	public Vec3 getGlobalVelocity(double t, Direction d, BlockPos launcher) {
		return VecHelper.rotate(new Vec3(0, dy(t), dx(t)), 180 + AngleHelper.horizontalAngle(d), Axis.Y);
	}

	public float x(double t) {
		return (float) (xMotion * -10.6033 * (-1 + Math.pow(0.91, t)));
	}

	public float y(double t) {
		double f = Math.pow(0.98, t);
		return (float) (yMotion * -49.4983 * f + 49.4983 * yMotion - 194.033 * f - 3.92 * t + 194.033);
	}

	public float dx(double t) {
		return (float) (xMotion * Math.pow(0.91, t));
	}

	public float dy(double t) {
		double f = Math.pow(0.98, t);
		return (float) (yMotion * f + ((f - 1) / (0.98 - 1)) * -0.0784);
	}

	protected void recalculateTrajectory() {
		double xTarget = this.horizontalDistance;
		double yTarget = this.verticalDistance;

		/*
		 ** Iterated:
		 * Horizontal Motion fh(x) = x * 0.91
		 * Vertical Motion fv(x) = (x - 0.08) * 0.98
		 * (Gravity modification ignored)
		 * > See LivingEntity.travel()
		 * 
		 ** n-th Iterative
		 * (= given initial velocity x, motion after t ticks)
		 * X'(x, t) = x * 0.91^t
		 * Y'(x, t) = x * 0.98^t + ((0.98^t - 1) / (0.98 - 1)) * -0.0784
		 * 
		 ** integral
		 * (= given intial velocity x, location offset after t ticks)
		 * X(x, t) = -10.6033x * (-1 + 0.91^t)
		 * Y(x, t) = -49.4983x * 0.98^t + 49.4983x - 194.033 * 0.98^t - 3.92t + 194.033
		 * 
		 ** argmax y
		 * (= given initial y velocity, ticks at which y reaches top)
		 * tPeak(x) = log(98 / (25x + 98)) / (2*log(7) - 2*log(5) - log(2))
		 * 
		 ** max y
		 * (= given initial y velocity, height offset at tPeak)
		 * yPeak(x) = 889.636 + 49.4983x + 0.032928/(98 + 25x) + 194.033 * log(1/(98 + 25x))
		 * 
		 ** yPeak inverse (Zelo's approximation)
		 * (= given yPeak, initial y velocity required to reach it at max)
		 * yMotion(h) = sqrt(2h/13) + 0.015h
		 * 
		 ** Y'(x, t) inverse (Simi's approximation)
		 * (= given yTarget and initial y velocity, elapsed ticks when arc hits yTarget on its way down)
		 * t*(x, v) = sqrt(yPeak(v) - x) * 4.87 + 0.115 * (yPeak(v) - x) + tPeak(v)
		 * 
		 ** xMotion
		 * (= given t* and xTarget, initial x velocity such that X'(x, t*) = xTarget)
		 * xMotion(t, x) = x / (-10.6033 * (-1 + 0.91^t));
		 * 
		 ** xError
		 * Interpolated correction function from observed inaccuracies in-game
		 * 
		 */

		double xError = (-0.0007 * Math.pow(xTarget + .5, 2) + 0.484)
			- (Math.min(5, yTarget) / 5) * Math.min(1, 0.076 * xTarget - 0.0014 * xTarget * xTarget);

		double yPeak = Math.max(0, yTarget + (xTarget + .5) / 8f) + (xTarget <= 1 ? 1 : 4);
		yMotion = Math.sqrt(2 * yPeak / 13) + 0.015 * yPeak;
		double tPeak = Math.log(98 / (25 * yMotion + 98)) / (2 * Math.log(7) - 2 * Math.log(5) - Math.log(2));
		totalFlyingTicks = Math.sqrt(yPeak - yMotion) * 4.87 + 0.115 * (yPeak - yMotion) + tPeak;
		xMotion = (xTarget - xError + .5) / (-10.6033 * (-1 + Math.pow(0.91, totalFlyingTicks)));

	}

}
