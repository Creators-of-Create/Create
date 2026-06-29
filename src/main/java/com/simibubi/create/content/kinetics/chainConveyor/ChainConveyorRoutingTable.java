package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.box.PackageItem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ChainConveyorRoutingTable {

	public static final int ENTRY_TIMEOUT = 100;
	public static final int PORT_ENTRY_TIMEOUT = 20;

	public record RoutingTableEntry(String port, int distance, BlockPos nextConnection, MutableInt timeout,
		boolean endOfRoute) {

		public void tick() {
			timeout.decrement();
		}

		public boolean invalid() {
			return timeout.intValue() <= 0;
		}

		public RoutingTableEntry copyForNeighbour(BlockPos connection) {
			return new RoutingTableEntry(port, distance + 1, connection.multiply(-1), new MutableInt(ENTRY_TIMEOUT),
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

	public void receivePortInfo(String filter, BlockPos connection) {
		insert(new RoutingTableEntry(filter, "*".equals(filter) ? 1000 : 0, connection, new MutableInt(PORT_ENTRY_TIMEOUT), true));
	}

	public BlockPos getExitFor(ItemStack box) {
		for (RoutingTableEntry entry : entriesByDistance)
			if (PackageItem.matchAddress(box, entry.port()))
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
		// Search our routing table for an entry with same port
		int targetIndex = 0;
		for (int i = 0; i < entriesByDistance.size(); i++) {
			RoutingTableEntry otherEntry = entriesByDistance.get(i);
			if (otherEntry.distance() > entry.distance())
				// Still not found at this distance : this either means it's an new port or a shorter route to a known port
				// In either case, we need to add the provided entry to our routing table
				break;
			if (otherEntry.port().equals(entry.port())) {
				if (otherEntry.distance() == entry.distance() && otherEntry.nextConnection().equals(entry.nextConnection()))
					// We know this port, and we are given a route we already have (same distant, same connection)
					// In that case, we can keep our existing entry and simply refresh its life time
					otherEntry.timeout.setValue(ENTRY_TIMEOUT);
				// ...else...
				// We know this port, but we are given a different route, either longer or going through a different connection
				// It's time to question our existing entry. As it might be obsolete, we let it decay (ie. we do nothing)
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
