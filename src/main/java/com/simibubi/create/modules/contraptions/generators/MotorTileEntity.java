package com.simibubi.create.modules.contraptions.generators;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.util.math.MathHelper;

public class MotorTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 64;
	public int newSpeed;
	public int lastModified;

	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
		setSpeed(DEFAULT_SPEED);
		lastModified = -1;
	}

	@Override
	public float getAddedStressCapacity() {
		return 500;
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean isSource() {
		return true;
	}

	@Override
	public void setSpeed(float speed) {
		super.setSpeed(speed);
		newSpeed = (int) speed;
	}
	
	@Override
	public void removeSource() {
		float speed = this.speed;
		super.removeSource();
		setSpeed(speed);
	}

	public int getSpeedValue() {
		if (world.isRemote)
			return newSpeed;
		return (int) speed;
	}

	public void setSpeedValueLazily(int speed) {
		if (newSpeed == speed)
			return;
		Integer max = CreateConfig.parameters.maxMotorSpeed.get();
		if (newSpeed > 0 && speed == 0)
			newSpeed = -1;
		else if (newSpeed < 0 && speed == 0)
			newSpeed = 1;
		else
			newSpeed = MathHelper.clamp(speed, -max, max);
		this.lastModified = 0;
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote)
			return;
		if (lastModified == -1)
			return;
		if (lastModified++ > 10) {
			lastModified = -1;
			AllPackets.channel.sendToServer(new ConfigureMotorPacket(pos, newSpeed));
		}
	}

}
