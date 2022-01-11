package com.simibubi.create.content.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class WaterWheelTileEntity extends KineticTileEntity {

	private Map<Direction, Float> flows;
	private float generated;

	public WaterWheelTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		flows = new HashMap<>();
		for (Direction d : Iterate.directions)
			setFlow(d, 0);
		setLazyTickRate(20);
	}

	@Override
	public void initialize() {
		updateGeneratedSpeed();
		super.initialize();
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (compound.contains("Flows")) {
			for (Direction d : Iterate.directions)
				setFlow(d, compound.getCompound("Flows")
					.getFloat(d.getSerializedName()));
		}
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		CompoundTag flows = new CompoundTag();
		for (Direction d : Iterate.directions)
			flows.putFloat(d.getSerializedName(), this.flows.get(d));
		compound.put("Flows", flows);

		super.write(compound, clientPacket);
	}

	public void setFlow(Direction direction, float speed) {
		flows.put(direction, speed);
		setChanged();
	}

	@Override
	public float getGeneratedSpeed() {
		return generated;
	}

	public void updateGeneratedSpeed() {
		generated = 0;
		for (Float f : flows.values())
			generated += f;
		if (generated != 0)
			generated += AllConfigs.SERVER.kinetics.waterWheelBaseSpeed.get() * Math.signum(generated);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		AllBlocks.WATER_WHEEL.get()
			.updateAllSides(getBlockState(), level, worldPosition);
	}

}
