package com.simibubi.create.modules.contraptions.generators;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class WaterWheelTileEntity extends KineticTileEntity {

	private Map<Direction, Integer> flows;
	private boolean hasFlows;

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
	public void onLoad() {
		super.onLoad();
//		updateSpeed();
	}

	public void updateSpeed() {
		float speed = 0;
		for (Integer i : flows.values())
			speed += i;

		if (this.speed != speed) {
			this.setSpeed(speed);
			hasFlows = speed != 0;
			notifyBlockUpdate();
			RotationPropagator.handleRemoved(world, pos, this);
			RotationPropagator.handleAdded(world, pos, this);
		}

	}

	@Override
	public boolean isSource() {
		return hasFlows;
	}

}
