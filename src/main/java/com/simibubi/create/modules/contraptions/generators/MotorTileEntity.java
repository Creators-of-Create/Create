package com.simibubi.create.modules.contraptions.generators;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.MathHelper;

public class MotorTileEntity extends KineticTileEntity implements ITickableTileEntity {

	public static final int MAX_SPEED = 4096;
	public static final int DEFAULT_SPEED = 64;
	public int newSpeed;
	public int lastModified;
	
	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
		setSpeed(DEFAULT_SPEED);
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
	
	public int getSpeedValue() {
		if (world.isRemote)
			return newSpeed;
		return (int) speed;
	}

	public void setSpeedValueLazily(int speed) {
		if (newSpeed == speed)
			return;
		newSpeed = MathHelper.clamp(speed, 1, MAX_SPEED);
		this.lastModified = 0;
	}

	@Override
	public void tick() {
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
