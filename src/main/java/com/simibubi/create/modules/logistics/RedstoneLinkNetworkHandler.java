package com.simibubi.create.modules.logistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;

public class RedstoneLinkNetworkHandler {

	static Map<IWorld, Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>>> connections = new HashMap<>();

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
			return stack.copy();
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

	public void onLoadWorld(IWorld world) {
		connections.put(world, new HashMap<>());
		Create.logger.debug("Prepared Redstone Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		connections.remove(world);
		Create.logger.debug("Removed Redstone Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public Set<LinkBehaviour> getNetworkOf(LinkBehaviour actor) {
		Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>> networksInWorld = networksIn(actor.getWorld());
		Pair<Frequency, Frequency> key = actor.getNetworkKey();
		if (!networksInWorld.containsKey(key))
			networksInWorld.put(key, new HashSet<>());
		return networksInWorld.get(key);
	}

	public void addToNetwork(LinkBehaviour actor) {
		getNetworkOf(actor).add(actor);
		updateNetworkOf(actor);
	}

	public void removeFromNetwork(LinkBehaviour actor) {
		Set<LinkBehaviour> network = getNetworkOf(actor);
		network.remove(actor);
		if (network.isEmpty()) {
			networksIn(actor.getWorld()).remove(actor.getNetworkKey());
			return;
		}
		updateNetworkOf(actor);
	}

	public void updateNetworkOf(LinkBehaviour actor) {
		Set<LinkBehaviour> network = getNetworkOf(actor);
		boolean powered = false;

		for (Iterator<LinkBehaviour> iterator = network.iterator(); iterator.hasNext();) {
			LinkBehaviour other = iterator.next();
			if (other.tileEntity.isRemoved()) {
				iterator.remove();
				continue;
			}
			if (actor.getWorld().getTileEntity(other.tileEntity.getPos()) != other.tileEntity) {
				iterator.remove();
				continue;
			}
			if (!withinRange(actor, other))
				continue;
			if (other.isTransmitting()) {
				powered = true;
				break;
			}
		}

		for (Iterator<LinkBehaviour> iterator = network.iterator(); iterator.hasNext();) {
			LinkBehaviour other = iterator.next();
			if (other.tileEntity.isRemoved()) {
				iterator.remove();
				continue;
			}
			if (!withinRange(actor, other))
				continue;
			if (other.isListening())
				other.updateReceiver(powered);
		}
	}

	public static boolean withinRange(LinkBehaviour from, LinkBehaviour to) {
		return from.getPos().withinDistance(to.getPos(), AllConfigs.SERVER.logistics.linkRange.get());
	}

	public Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>> networksIn(IWorld world) {
		if (!connections.containsKey(world)) {
			Create.logger.warn(
					"Tried to Access unprepared network space of " + world.getDimension().getType().getRegistryName());
			return new HashMap<>();
		}
		return connections.get(world);
	}

}
