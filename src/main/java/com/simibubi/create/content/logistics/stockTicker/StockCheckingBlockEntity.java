package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StockCheckingBlockEntity extends SmartBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;

	public StockCheckingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, false));
	}

	public InventorySummary getRecentSummary() {
		return LogisticsManager.getSummaryOfNetwork(behaviour.freqId, false);
	}

	public InventorySummary getAccurateSummary() {
		return LogisticsManager.getSummaryOfNetwork(behaviour.freqId, true);
	}

	public boolean broadcastPackageRequest(RequestType type, PackageOrder order, @Nullable IdentifiedInventory ignoredHandler, String address) {
		return broadcastPackageRequest(type, PackageOrderWithCrafts.simple(order.stacks()), ignoredHandler, address);
	}

	public boolean broadcastPackageRequest(RequestType type, PackageOrderWithCrafts order, @Nullable IdentifiedInventory ignoredHandler, String address) {
		return LogisticsManager.broadcastPackageRequest(behaviour.freqId, type, order, ignoredHandler, address);
	}

}
