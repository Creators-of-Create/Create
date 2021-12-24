package com.simibubi.create.content.logistics.block.depot;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.ItemTransferable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DepotTileEntity extends SmartTileEntity implements ItemTransferable {

	DepotBehaviour depotBehaviour;

	public DepotTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(depotBehaviour = new DepotBehaviour(this));
		depotBehaviour.addSubBehaviours(behaviours);
	}

	@Nullable
	@Override
	public IItemHandler getItemHandler(@Nullable Direction direction) {
		return depotBehaviour.lazyItemHandler.orElse(null);
	}
}
