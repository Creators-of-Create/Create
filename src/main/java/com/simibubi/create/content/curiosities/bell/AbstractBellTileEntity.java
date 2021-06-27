package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public abstract class AbstractBellTileEntity extends SmartTileEntity {

	public static final int RING_DURATION = 50;

	public boolean isRinging;
	public int ringingTicks;
	public Direction ringDirection;

	public AbstractBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	public boolean ring(World world, BlockPos pos, Direction direction) {
		isRinging = true;
		ringingTicks = 0;
		ringDirection = direction;
		return true;
	};

	@Override
	public void tick() {
		super.tick();

		if (isRinging) {
			++ringingTicks;
		}

		if (ringingTicks >= RING_DURATION) {
			isRinging = false;
			ringingTicks = 0;
		}
	}

}
