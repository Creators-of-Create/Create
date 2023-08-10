package com.simibubi.create.content.redstone.link.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class LinkedControllerServerHandler {

	public static WorldAttached<Map<UUID, Collection<ManualFrequencyEntry>>> receivedInputs =
		new WorldAttached<>($ -> new HashMap<>());
	static final int TIMEOUT = 30;

	public static void tick(LevelAccessor world) {
		Map<UUID, Collection<ManualFrequencyEntry>> map = receivedInputs.get(world);
		for (Iterator<Entry<UUID, Collection<ManualFrequencyEntry>>> iterator = map.entrySet()
			.iterator(); iterator.hasNext();) {

			Entry<UUID, Collection<ManualFrequencyEntry>> entry = iterator.next();
			Collection<ManualFrequencyEntry> list = entry.getValue();

			for (Iterator<ManualFrequencyEntry> entryIterator = list.iterator(); entryIterator.hasNext();) {
				ManualFrequencyEntry manualFrequencyEntry = entryIterator.next();
				manualFrequencyEntry.decrement();
				if (!manualFrequencyEntry.isAlive()) {
					Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(world, manualFrequencyEntry);
					entryIterator.remove();
				}
			}

			if (list.isEmpty())
				iterator.remove();
		}
	}

	public static void receivePressed(LevelAccessor world, BlockPos pos, UUID uniqueID, List<Couple<Frequency>> collect,
		boolean pressed) {
		Map<UUID, Collection<ManualFrequencyEntry>> map = receivedInputs.get(world);
		Collection<ManualFrequencyEntry> list = map.computeIfAbsent(uniqueID, $ -> new ArrayList<>());

		WithNext: for (Couple<Frequency> activated : collect) {
			for (Iterator<ManualFrequencyEntry> iterator = list.iterator(); iterator.hasNext();) {
				ManualFrequencyEntry entry = iterator.next();
				if (entry.getSecond()
					.equals(activated)) {
					if (!pressed)
						entry.setFirst(0);
					else
						entry.updatePosition(pos);
					continue WithNext;
				}
			}

			if (!pressed)
				continue;

			ManualFrequencyEntry entry = new ManualFrequencyEntry(pos, activated);
			Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(world, entry);
			list.add(entry);

			for (IRedstoneLinkable linkable : Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(world, entry))
				if (linkable instanceof LinkBehaviour lb && lb.isListening())
					AllAdvancements.LINKED_CONTROLLER.awardTo(world.getPlayerByUUID(uniqueID));
		}
	}

	static class ManualFrequencyEntry extends IntAttached<Couple<Frequency>> implements IRedstoneLinkable {

		private BlockPos pos;

		public ManualFrequencyEntry(BlockPos pos, Couple<Frequency> second) {
			super(TIMEOUT, second);
			this.pos = pos;
		}

		public void updatePosition(BlockPos pos) {
			this.pos = pos;
			setFirst(TIMEOUT);
		}

		@Override
		public int getTransmittedStrength() {
			return isAlive() ? 15 : 0;
		}

		@Override
		public boolean isAlive() {
			return getFirst() > 0;
		}

		@Override
		public BlockPos getLocation() {
			return pos;
		}

		@Override
		public void setReceivedStrength(int power) {}

		@Override
		public boolean isListening() {
			return false;
		}

		@Override
		public Couple<Frequency> getNetworkKey() {
			return getSecond();
		}

	}

}
