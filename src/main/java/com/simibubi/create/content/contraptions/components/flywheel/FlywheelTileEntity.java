package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

public class FlywheelTileEntity extends GeneratingKineticTileEntity {

	private float generatedCapacity;
	private float generatedSpeed;
	private int stoppingCooldown;

	// Client
	InterpolatedChasingValue visualSpeed = new InterpolatedChasingValue();
	float angle;

	public FlywheelTileEntity(TileEntityType<? extends FlywheelTileEntity> type) {
		super(type);
	}

	public void setRotation(float speed, float capacity) {
		if (generatedSpeed != speed || generatedCapacity != capacity) {

			if (speed == 0) {
				if (stoppingCooldown == 0)
					stoppingCooldown = 40;
				return;
			}

			stoppingCooldown = 0;
			generatedSpeed = speed;
			generatedCapacity = capacity;
			updateGeneratedRotation();
		}
	}

	@Override
	public float getGeneratedSpeed() {
		return convertToDirection(generatedSpeed, getBlockState().get(FlywheelBlock.HORIZONTAL_FACING));
	}

	@Override
	public float calculateAddedStressCapacity() {
		return generatedCapacity;
	}

	@Override
	public AxisAlignedBB makeRenderBoundingBox() {
		return super.makeRenderBoundingBox().grow(2);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("GeneratedSpeed", generatedSpeed);
		compound.putFloat("GeneratedCapacity", generatedCapacity);
		compound.putInt("Cooldown", stoppingCooldown);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		generatedSpeed = compound.getFloat("GeneratedSpeed");
		generatedCapacity = compound.getFloat("GeneratedCapacity");
		stoppingCooldown = compound.getInt("Cooldown");
		super.read(compound, clientPacket);
		if (clientPacket)
			visualSpeed.withSpeed(1 / 32f)
				.target(getGeneratedSpeed());
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote) {
			visualSpeed.target(getGeneratedSpeed());
			visualSpeed.tick();
			angle += visualSpeed.value * 3 / 10f;
			angle %= 360;
			return;
		}

		/*
		 * After getting moved by pistons the generatedSpeed attribute reads 16 but the
		 * actual speed stays at 0, if it happens update rotation
		 */
		if (getGeneratedSpeed() != 0 && getSpeed() == 0)
			updateGeneratedRotation();

		if (stoppingCooldown == 0)
			return;

		stoppingCooldown--;
		if (stoppingCooldown == 0) {
			generatedCapacity = 0;
			generatedSpeed = 0;
			updateGeneratedRotation();
		}
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}
}
