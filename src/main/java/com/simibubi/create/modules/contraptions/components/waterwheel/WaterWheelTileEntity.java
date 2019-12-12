package com.simibubi.create.modules.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

	private Map<Direction, Integer> flows;

	public WaterWheelTileEntity() {
		super(AllTileEntities.WATER_WHEEL.type);
		flows = new HashMap<Direction, Integer>();
		for (Direction d : Direction.values())
			setFlow(d, 0);

	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		if (compound.contains("Flows")) {
			for (Direction d : Direction.values())
				setFlow(d, compound.getCompound("Flows").getInt(d.getName()));
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {

		CompoundNBT flows = new CompoundNBT();
		for (Direction d : Direction.values())
			flows.putInt(d.getName(), this.flows.get(d));
		compound.put("Flows", flows);

		return super.write(compound);
	}

	public void setFlow(Direction direction, int speed) {
		flows.put(direction, speed);
	}

	@Override
	public float getGeneratedSpeed() {
		float speed = 0;
		for (Integer i : flows.values())
			speed += i;
		return speed;
	}

}
