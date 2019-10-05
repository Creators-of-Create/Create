package com.simibubi.create.modules.logistics.management;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.simibubi.create.modules.logistics.management.base.LogisticalControllerTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.DepositTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.SupplyTask;
import com.simibubi.create.modules.logistics.management.controller.TransactionsTileEntity;
import com.simibubi.create.modules.logistics.management.index.LogisticalIndexTileEntity;

public class LogisticalNetwork {

	public List<TransactionsTileEntity> taskQueues = new ArrayList<>();
	public List<LogisticalIndexTileEntity> indexers = new ArrayList<>();
	public PriorityQueue<LogisticalTask> internalTaskQueue = new PriorityQueue<>();
	public PriorityQueue<LogisticalControllerTileEntity> suppliers = new PriorityQueue<>();
	public PriorityQueue<LogisticalControllerTileEntity> receivers = new PriorityQueue<>();
	public int participants = 0;
	public boolean tasksUpdated;

	public void addController(LogisticalControllerTileEntity te) {
		if (te instanceof TransactionsTileEntity) {
			if (taskQueues.contains(te))
				return;
			taskQueues.add((TransactionsTileEntity) te);
		}
		if (te instanceof LogisticalIndexTileEntity) {
			if (indexers.contains(te))
				return;
			indexers.add((LogisticalIndexTileEntity) te);
		}
		if (te.isSupplier()) {
			if (suppliers.contains(te))
				return;
			suppliers.add(te);
		}
		if (te.isReceiver()) {
			if (receivers.contains(te))
				return;
			receivers.add(te);
			indexers.forEach(LogisticalIndexTileEntity::syncReceivers);
		}
		participants++;
	}

	public void removeController(LogisticalControllerTileEntity te) {
		if (te instanceof TransactionsTileEntity)
			if (!taskQueues.remove((TransactionsTileEntity) te))
				return;
		if (te instanceof LogisticalIndexTileEntity)
			if (!indexers.remove((LogisticalIndexTileEntity) te))
				return;
		if (te.isSupplier())
			if (!suppliers.remove(te))
				return;
		if (te.isReceiver()) {
			if (!receivers.remove(te))
				return;
			indexers.forEach(LogisticalIndexTileEntity::syncReceivers);
		}
		participants--;
	}

	public boolean isEmpty() {
		return participants == 0;
	}

	public void enqueueTask(LogisticalTask task) {
		internalTaskQueue.add(task);
		if (task instanceof SupplyTask)
			suppliers.forEach(LogisticalControllerTileEntity::notifyTaskUpdate);
		if (task instanceof DepositTask)
			receivers.forEach(LogisticalControllerTileEntity::notifyTaskUpdate);
	}

}
