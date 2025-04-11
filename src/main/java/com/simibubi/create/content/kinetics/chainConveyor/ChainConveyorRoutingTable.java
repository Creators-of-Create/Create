package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.simibubi.create.foundation.utility.LogisticParser;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.box.PackageItem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ChainConveyorRoutingTable {

	public static final int ENTRY_TIMEOUT = 100;
	public static final int PORT_ENTRY_TIMEOUT = 20;

	public record RoutingTableEntry(String port, boolean useRegex, int distance, BlockPos nextConnection, MutableInt timeout,
		boolean endOfRoute) {

		public void tick() {
			timeout.decrement();
		}

		public boolean invalid() {
			return timeout.intValue() <= 0;
		}

		public RoutingTableEntry copyForNeighbour(BlockPos connection) {
			return new RoutingTableEntry(port, useRegex, distance + 1, connection.multiply(-1), new MutableInt(ENTRY_TIMEOUT),
				false);
		}

	}

	public List<RoutingTableEntry> entriesByDistance = new ArrayList<>();
	public int lastUpdate;
	public boolean changed;

	public void tick() {
		entriesByDistance.forEach(RoutingTableEntry::tick);
		changed |= entriesByDistance.removeIf(RoutingTableEntry::invalid);
		lastUpdate++;
	}

	public boolean shouldAdvertise() {
		return changed || lastUpdate > ENTRY_TIMEOUT - 20;
	}

	public void receivePortInfo(String filter, boolean usesRegex, BlockPos connection) {
		insert(new RoutingTableEntry(filter, usesRegex, LogisticParser.matchesAll(filter, usesRegex) ? 1000 : 0, connection, new MutableInt(PORT_ENTRY_TIMEOUT), true));
	}

	public BlockPos getExitFor(ItemStack box) {
		for (RoutingTableEntry entry : entriesByDistance)
			if (PackageItem.matchAddress(box, entry.port(), entry.useRegex()))
				return entry.nextConnection();
		return BlockPos.ZERO;
	}

	public void advertiseTo(BlockPos connection, ChainConveyorRoutingTable otherTable) {
		BlockPos backConnection = connection.multiply(-1);
		for (RoutingTableEntry entry : entriesByDistance)
			if (entry.endOfRoute() || !connection.equals(entry.nextConnection()))
				otherTable.insert(entry.copyForNeighbour(connection));
		otherTable.entriesByDistance.removeIf(e -> e.timeout()
			.intValue() < ENTRY_TIMEOUT && !e.endOfRoute() && backConnection.equals(e.nextConnection()));
	}

	private void insert(RoutingTableEntry entry) {
		int targetIndex = 0;
		for (int i = 0; i < entriesByDistance.size(); i++) {
			RoutingTableEntry otherEntry = entriesByDistance.get(i);
			if (otherEntry.distance() > entry.distance())
				break;
			if (otherEntry.port()
				.equals(entry.port())) {
				otherEntry.timeout.setValue(ENTRY_TIMEOUT);
				return;
			}
			targetIndex = i + 1;
		}
		entriesByDistance.add(targetIndex, entry);
		changed = true;
	}

	public Collection<? extends Component> createSummary() {
		ArrayList<Component> list = new ArrayList<>();
		for (RoutingTableEntry entry : entriesByDistance) {
            list.add(Component.literal("    [" + entry.distance() + "] " + entry.port()));
        }
		return list;
	}

}
