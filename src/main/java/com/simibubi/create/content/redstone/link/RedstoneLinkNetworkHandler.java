package com.simibubi.create.content.redstone.link;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

public class RedstoneLinkNetworkHandler {

	static final Map<LevelAccessor, RedstoneLinkNetworkHandlerSavedData> connections = new IdentityHashMap<>();

	public final AtomicInteger globalPowerVersion = new AtomicInteger();

	public static class Frequency {

		public static final Frequency EMPTY = new Frequency(ItemStack.EMPTY);
		private static final Map<Item, Frequency> simpleFrequencies = new IdentityHashMap<>();
		final private ItemStack stack;
		final private Item item;
		final private int color;

		public static Frequency of(ItemStack stack) {
			if (stack.isEmpty()) return EMPTY;
			if (stack.getComponents().isEmpty()) return simpleFrequencies.computeIfAbsent(stack.getItem(), $ -> new Frequency(stack));
			return new Frequency(stack);
		}

		@SuppressWarnings("DataFlowIssue")
		private Frequency(ItemStack stack) {
			this.stack = stack;
			item = stack.getItem();
			color = stack.has(DataComponents.DYED_COLOR) ? stack.get(DataComponents.DYED_COLOR).rgb() : -1;
		}

		public ItemStack getStack() {
			return stack;
		}

		@Override
		public int hashCode() {
			return (item.hashCode() * 31) ^ color;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return obj instanceof Frequency && ((Frequency) obj).item == item && ((Frequency) obj).color == color;
		}
	}

	public void onLoadWorld(final LevelAccessor world) {
		if (world instanceof ServerLevel serverLevel) {
			connections.put(world, serverLevel.getDataStorage().computeIfAbsent(RedstoneLinkNetworkHandlerSavedData.factory(), "create_redstone_link_network"));
		}

		Create.LOGGER.debug("Prepared Redstone Network Space for {}", WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(final LevelAccessor world) {
		connections.remove(world);
		Create.LOGGER.debug("Removed Redstone Network Space for {}", WorldHelper.getDimensionID(world));
	}

	public RedstoneLinkNetwork getNetworkOf(final LevelAccessor world, final IRedstoneLinkable actor) {
		return connections.get(world).getNetwork(actor.getNetworkKey());
	}

	public void addToNetwork(final LevelAccessor world, final IRedstoneLinkable actor) {
		final RedstoneLinkNetwork network = getNetworkOf(world, actor);
		if (actor.isListening()) {
			network.addReceiver(actor);
			updateReceiver(network, actor);
		} else {
			globalPowerVersion.incrementAndGet();
			network.addTransmitter(actor);
			handleTransmitterAdd(network, actor);
		}
	}

	public void removeFromNetwork(final LevelAccessor world, final IRedstoneLinkable actor) {
		final RedstoneLinkNetwork network = getNetworkOf(world, actor);
		if (actor.isListening()) {
			network.getReceivers().remove(actor);
		} else {
			globalPowerVersion.incrementAndGet();
			network.getTransmitters().remove(actor);
			handleTransmitterRemove(network, actor);
		}
	}

	public void markUnloaded(final LevelAccessor world, final IRedstoneLinkable actor) {
		if (actor.isListening()) {
			removeFromNetwork(world, actor);
		} else {
			final RedstoneLinkNetwork network = getNetworkOf(world, actor);
			network.markUnloaded(actor);
			connections.get(world).setDirty();
		}
	}

	public void transmitterSignalChanged(final LevelAccessor world, final IRedstoneLinkable actor, final int oldSignal) {
		globalPowerVersion.incrementAndGet();
		final int power = actor.getTransmittedStrength();
		final boolean isIncrease = oldSignal < power;
		final RedstoneLinkNetwork network = getNetworkOf(world, actor);

		for (Iterator<IRedstoneLinkable> iterator = network.getReceivers().iterator(); iterator.hasNext(); ) {
			final IRedstoneLinkable other = iterator.next();
			if (!other.isAlive()) {
				iterator.remove();
				continue;
			}

			// Receiver is either already receiving a higher or equal signal or wasn't powered by this transmitter to begin with, so we can continue early
			if ((isIncrease && other.getReceivedStrength() >= power) || (!isIncrease && other.getReceivedStrength() != oldSignal) || !withinRange(actor, other)) {
				continue;
			}
			updateReceiver(network, other);
		}
	}

	private void updateReceiver(final RedstoneLinkNetwork network, final IRedstoneLinkable actor) {
		int power = 0;
		for (Iterator<IRedstoneLinkable> iterator = network.getTransmitters().iterator(); iterator.hasNext(); ) {
			final IRedstoneLinkable other = iterator.next();
			if (!other.isAlive()) {
				iterator.remove();
				continue;
			}

			if (!withinRange(actor, other)) continue;

			power = Math.max(other.getTransmittedStrength(), power);
			if (power >= 15) {
				break;
			}
		}
		actor.setReceivedStrength(power);
	}

	private void handleTransmitterAdd(final RedstoneLinkNetwork network, final IRedstoneLinkable actor) {
		if (actor.getTransmittedStrength() == 0) return;
		final int power = actor.getTransmittedStrength();

		for (Iterator<IRedstoneLinkable> iterator = network.getReceivers().iterator(); iterator.hasNext(); ) {
			final IRedstoneLinkable other = iterator.next();
			if (!other.isAlive()) {
				iterator.remove();
				continue;
			}

			// Receiver already receiving higher or equal strength, so we can continue early
			if (power <= other.getReceivedStrength() || !withinRange(actor, other)) continue;
			updateReceiver(network, other);
		}
	}

	private void handleTransmitterRemove(final RedstoneLinkNetwork network, final IRedstoneLinkable actor) {
		if (actor.getTransmittedStrength() == 0) return;
		final int power = actor.getTransmittedStrength();

		for (Iterator<IRedstoneLinkable> iterator = network.getReceivers().iterator(); iterator.hasNext(); ) {
			final IRedstoneLinkable other = iterator.next();
			if (!other.isAlive()) {
				iterator.remove();
				continue;
			}

			// Transmitter was definitely not source, so we can continue early
			if (power != other.getReceivedStrength() || !withinRange(actor, other)) continue;
			updateReceiver(network, other);
		}
	}

	public static boolean withinRange(IRedstoneLinkable from, IRedstoneLinkable to) {
		if (from == to) return true;
		return from.getLocation().closerThan(to.getLocation(), AllConfigs.server().logistics.linkRange.get());
	}

	public boolean hasAnyLoadedPower(Couple<Frequency> frequency) {
		for (final RedstoneLinkNetworkHandlerSavedData savedData : connections.values()) {
			final Set<IRedstoneLinkable> set = savedData.getNetwork(frequency).getTransmitters();
			if (set.isEmpty()) continue;
			for (IRedstoneLinkable link : set) {
				if (link.getTransmittedStrength() > 0) return true;
			}
		}
		return false;
	}
}
