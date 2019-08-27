package com.simibubi.create.modules.logistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FrequencyHandler {

	public static final int RANGE = 128;

	static Map<IWorld, Map<Pair<Frequency, Frequency>, List<IHaveWireless>>> connections = new HashMap<>();

	public static class Frequency {
		private ItemStack stack;
		private Item item;
		private int color;

		public Frequency(ItemStack stack) {
			this.stack = stack;
			item = stack.getItem();
			CompoundNBT displayTag = stack.getChildTag("display");
			color = displayTag != null && displayTag.contains("color") ? displayTag.getInt("color") : -1;
		}

		public ItemStack getStack() {
			return stack;
		}

		@Override
		public int hashCode() {
			return item.hashCode() ^ color;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Frequency ? ((Frequency) obj).item == item && ((Frequency) obj).color == color
					: false;
		}

	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		connections.put(event.getWorld(), new HashMap<>());
		Create.logger.info("Prepared network space for " + event.getWorld().getDimension().getType().getRegistryName());
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		connections.remove(event.getWorld());
		Create.logger.info("Removed network space for " + event.getWorld().getDimension().getType().getRegistryName());
	}

	private static Pair<Frequency, Frequency> getNetworkKey(IHaveWireless actor) {
		return Pair.of(actor.getFrequencyFirst(), actor.getFrequencyLast());
	}

	public static List<IHaveWireless> getNetworkOf(IHaveWireless actor) {
		Map<Pair<Frequency, Frequency>, List<IHaveWireless>> networksInWorld = networksIn(actor.getWorld());
		Pair<Frequency, Frequency> key = getNetworkKey(actor);
		if (!networksInWorld.containsKey(key))
			networksInWorld.put(key, new ArrayList<>());
		return networksInWorld.get(key);
	}

	public static void addToNetwork(IHaveWireless actor) {
		getNetworkOf(actor).add(actor);
		updateNetworkOf(actor);
	}

	public static void removeFromNetwork(IHaveWireless actor) {
		List<IHaveWireless> network = getNetworkOf(actor);
		network.remove(actor);
		if (network.isEmpty()) {
			networksIn(actor.getWorld()).remove(getNetworkKey(actor));
			return;
		}
		updateNetworkOf(actor);
	}

	public static void updateNetworkOf(IHaveWireless actor) {
		List<IHaveWireless> network = getNetworkOf(actor);
		boolean powered = false;

		// Update from Transmitters
		for (IHaveWireless other : network) {
			if (!other.isLoaded() || !withinRange(actor, other))
				continue;
			if (other instanceof ITransmitWireless && ((ITransmitWireless) other).getSignal()) {
				powered = true;
				break;
			}
		}

		// Update the Receivers
		for (IHaveWireless other : network) {
			if (!other.isLoaded() || !withinRange(actor, other))
				continue;
			if (other instanceof IReceiveWireless)
				((IReceiveWireless) other).setSignal(powered);
		}
	}

	public static boolean withinRange(IHaveWireless from, IHaveWireless to) {
		return from.getPos().withinDistance(to.getPos(), RANGE);
	}

	public static Map<Pair<Frequency, Frequency>, List<IHaveWireless>> networksIn(IWorld world) {
		if (!connections.containsKey(world)) {
			Create.logger.warn(
					"Tried to Access unprepared network space of " + world.getDimension().getType().getRegistryName());
			return new HashMap<>();
		}
		return connections.get(world);
	}

}
