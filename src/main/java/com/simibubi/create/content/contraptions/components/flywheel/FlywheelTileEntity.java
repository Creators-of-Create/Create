package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;

public class FlywheelTileEntity extends GeneratingKineticTileEntity {

	private float generatedCapacity;
	private float generatedSpeed;
	private int stoppingCooldown;

	// Client
	InterpolatedChasingValue visualSpeed = new InterpolatedChasingValue();
	float angle;

	public FlywheelTileEntity() {
		super(AllTileEntities.FLYWHEEL.type);
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
		return generatedSpeed;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return generatedCapacity;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().grow(2);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		visualSpeed.withSpeed(1 / 32f).target(generatedSpeed);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putFloat("GeneratedSpeed", generatedSpeed);
		compound.putFloat("GeneratedCapacity", generatedCapacity);
		compound.putInt("Cooldown", stoppingCooldown);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		generatedSpeed = compound.getFloat("GeneratedSpeed");
		generatedCapacity = compound.getFloat("GeneratedCapacity");
		stoppingCooldown = compound.getInt("Cooldown");
		super.read(compound);
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote) {
			visualSpeed.target(generatedSpeed);
			visualSpeed.tick();
			angle += visualSpeed.value * 3 / 10f;
			angle %= 360;
			return;
		}
		if (stoppingCooldown == 0)
			return;

		stoppingCooldown--;
		if (stoppingCooldown == 0) {
			generatedCapacity = 0;
			generatedSpeed = 0;
			updateGeneratedRotation();
		}
	}

}
