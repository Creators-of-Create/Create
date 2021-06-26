package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public abstract class AbstractBellTileEntity extends SmartTileEntity {

	public AbstractBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	public abstract boolean ring(World world, BlockPos pos, Direction direction);

}
