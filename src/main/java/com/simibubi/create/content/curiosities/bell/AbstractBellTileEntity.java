package com.simibubi.create.content.curiosities.bell;

import java.util.List;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class AbstractBellTileEntity extends SmartTileEntity {

	public static final int RING_DURATION = 74;

	public boolean isRinging;
	public int ringingTicks;
	public Direction ringDirection;

	public AbstractBellTileEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	public boolean ring(Level world, BlockPos pos, Direction direction) {
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
