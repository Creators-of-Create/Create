package com.simibubi.create.content.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

	private Map<Direction, Float> flows;

	public WaterWheelTileEntity() {
		super(AllTileEntities.WATER_WHEEL.type);
		flows = new HashMap<>();
		for (Direction d : Direction.values())
			setFlow(d, 0);
		setLazyTickRate(20);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		if (compound.contains("Flows")) {
			for (Direction d : Direction.values())
				setFlow(d, compound.getCompound("Flows")
					.getFloat(d.getName()));
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
			flows.putFloat(d.getName(), this.flows.get(d));
		compound.put("Flows", flows);

		return super.write(compound);
	}

	public void setFlow(Direction direction, float speed) {
		flows.put(direction, speed);
		markDirty();
	}

	@Override
	public float getGeneratedSpeed() {
		float speed = 0;
		for (Float f : flows.values())
			speed += f;
		return speed;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		AllBlocks.WATER_WHEEL.get()
			.updateAllSides(getBlockState(), world, pos);
	}

}
