package com.simibubi.create.content.curiosities.bell;

import java.util.List;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractBellTileEntity extends SmartTileEntity {

	public static final int RING_DURATION = 74;

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

	public abstract PartialModel getBellModel();

}
