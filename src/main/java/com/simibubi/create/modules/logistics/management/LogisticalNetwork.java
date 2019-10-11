package com.simibubi.create.modules.logistics.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.DepositTask;
import com.simibubi.create.modules.logistics.management.base.LogisticalTask.SupplyTask;
import com.simibubi.create.modules.logistics.management.controller.StorageTileEntity;
import com.simibubi.create.modules.logistics.management.controller.TransactionsTileEntity;
import com.simibubi.create.modules.logistics.management.index.LogisticalIndexTileEntity;
import com.simibubi.create.modules.logistics.transport.villager.PackageFunnelTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class LogisticalNetwork {

	public List<TransactionsTileEntity> taskQueues = new ArrayList<>();
	public List<LogisticalIndexTileEntity> indexers = new ArrayList<>();
	public Set<PackageFunnelTileEntity> packageTargets = new HashSet<>();
	public PriorityQueue<LogisticalTask> internalTaskQueue = new PriorityQueue<>();
	public PriorityQueue<LogisticalActorTileEntity> suppliers = new PriorityQueue<>();
	public PriorityQueue<LogisticalActorTileEntity> receivers = new PriorityQueue<>();
	public int participants = 0;
	public boolean tasksUpdated;

	public void addController(LogisticalActorTileEntity te) {
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
			reAdvertiseReceivers();
		}
		participants++;
	}

	public void addPackageTarget(PackageFunnelTileEntity te) {
		packageTargets.add(te);
	}

	public void removePackageTarget(PackageFunnelTileEntity te) {
		packageTargets.remove(te);
	}

	public void reAdvertiseReceivers() {
		indexers.forEach(LogisticalIndexTileEntity::syncReceivers);
	}

	public void removeController(LogisticalActorTileEntity te) {
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
			reAdvertiseReceivers();
		}
		participants--;
	}

	public boolean isEmpty() {
		return participants == 0;
	}

	public void enqueueTask(LogisticalTask task) {
		internalTaskQueue.add(task);
		
		Minecraft.getInstance().player.sendMessage(new StringTextComponent(internalTaskQueue.toString()));
		
		if (task instanceof SupplyTask)
			suppliers.forEach(LogisticalActorTileEntity::notifyTaskUpdate);
		if (task instanceof DepositTask)
			receivers.forEach(LogisticalActorTileEntity::notifyTaskUpdate);
	}

	public String getNextAvailableAddress(LogisticalActorTileEntity te) {
		Predicate<String> isTaken = s -> false;
		String prefix = "";

		if (te instanceof TransactionsTileEntity) {
			prefix = "Task Manager";
			isTaken = s -> isNameTaken(taskQueues, s);
		}

		else if (te instanceof LogisticalIndexTileEntity) {
			prefix = "Index";
			isTaken = s -> isNameTaken(indexers, s);
		}

		else if (te instanceof StorageTileEntity) {
			prefix = "Storage";
			isTaken = s -> isNameTaken(suppliers, s);
		}

		else if (te.isSupplier()) {
			prefix = "Supply";
			isTaken = s -> isNameTaken(suppliers, s);
		}

		else if (te.isReceiver()) {
			prefix = "Request";
			isTaken = s -> isNameTaken(receivers, s);
		}

		int i = 0;
		String name;
		do {
			name = prefix + (i == 0 ? "" : " " + i);
			i++;
		} while (isTaken.test(name));

		return name;
	}

	private static <T extends LogisticalActorTileEntity> boolean isNameTaken(Collection<T> list, String name) {
		for (T controller : list)
			if (controller.address.equals(name))
				return true;
		return false;
	}
	
	public static boolean matchAddresses(String addr1, String addr2) {
		return addr1.toLowerCase().equals(addr2.toLowerCase());
	}

}
