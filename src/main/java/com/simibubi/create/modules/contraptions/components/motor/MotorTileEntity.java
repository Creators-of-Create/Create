package com.simibubi.create.modules.contraptions.components.motor;

import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class MotorTileEntity extends GeneratingKineticTileEntity {

	public static final int DEFAULT_SPEED = 64;
	public int newGeneratedSpeed;
	public int generatedSpeed;
	public int lastModified;

	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
		speed = generatedSpeed = newGeneratedSpeed = DEFAULT_SPEED;
		updateNetwork = true;
		newNetworkID = UUID.randomUUID();
		lastModified = -1;
	}

	@Override
	public float getGeneratedSpeed() {
		return generatedSpeed;
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("GeneratedSpeed", generatedSpeed);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		generatedSpeed = compound.getInt("GeneratedSpeed");
		if (lastModified == -1)
			newGeneratedSpeed = generatedSpeed;
		super.read(compound);
	}

	public void setSpeedValueLazily(int speed) {
		if (newGeneratedSpeed == speed)
			return;
		Integer max = CreateConfig.parameters.maxMotorSpeed.get();
		if (newGeneratedSpeed > 0 && speed == 0)
			newGeneratedSpeed = -1;
		else if (newGeneratedSpeed < 0 && speed == 0)
			newGeneratedSpeed = 1;
		else
			newGeneratedSpeed = MathHelper.clamp(speed, -max, max);
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
			AllPackets.channel.sendToServer(new ConfigureMotorPacket(pos, newGeneratedSpeed));
		}
	}

}
