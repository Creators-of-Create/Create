package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.content.contraptions.base.IVisualRotationWheel;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FlywheelTileEntity extends KineticTileEntity implements IVisualRotationWheel {

	boolean hasForcedSpeed = false;
	float forcedSpeed = 0;

	LerpedFloat visualSpeed = LerpedFloat.linear();
	float angle;

	public FlywheelTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket)
			visualSpeed.chase(getGeneratedSpeed(), 1 / 64f, Chaser.EXP);
	}

	@Override
	public float getSpeed() {
		return hasForcedSpeed ? forcedSpeed : super.getSpeed();
	}

	@Override
	public void tick() {
		super.tick();

		if (!level.isClientSide)
			return;

		float targetSpeed = getSpeed();
		visualSpeed.updateChaseTarget(targetSpeed);
		visualSpeed.tickChaser();
		angle += visualSpeed.getValue() * 3 / 10f;
		angle %= 360;
	}

	public void setForcedSpeed(float speed) {
		hasForcedSpeed = true;
		forcedSpeed = speed;
		visualSpeed.updateChaseTarget(speed);
		visualSpeed.tickChaser();
	}

	public void unsetForcedSpeed() {
		hasForcedSpeed = false;
	}

	@Override
	public void setAngle(float angle) {
		this.angle = angle;
	}

	@Override
	public float getAngle() {
		return angle;
	}

	@Override
	public float getWheelRadius() {
		return 22.5f / 16;
	}
}
