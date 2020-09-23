package com.simibubi.create.content.logistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.foundation.utility.WorldHelper;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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
		Create.logger.debug("Prepared Redstone Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(IWorld world) {
		connections.remove(world);
		Create.logger.debug("Removed Redstone Network Space for " + WorldHelper.getDimensionID(world));
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
		int power = 0;

		for (Iterator<LinkBehaviour> iterator = network.iterator(); iterator.hasNext();) {
			LinkBehaviour other = iterator.next();
			if (other.tileEntity.isRemoved()) {
				iterator.remove();
				continue;
			}
			World world = actor.getWorld();
			if (!world.isBlockPresent(other.tileEntity.getPos())) {
				iterator.remove();
				continue;
			}
			if (world.getTileEntity(other.tileEntity.getPos()) != other.tileEntity) {
				iterator.remove();
				continue;
			}
			if (!withinRange(actor, other))
				continue;
			power = Math.max(other.getTransmittedStrength(), power);
			if (power == 15)
				break;
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
				other.updateReceiver(power);
		}
	}

	public static boolean withinRange(LinkBehaviour from, LinkBehaviour to) {
		return from.getPos().withinDistance(to.getPos(), AllConfigs.SERVER.logistics.linkRange.get());
	}

	public Map<Pair<Frequency, Frequency>, Set<LinkBehaviour>> networksIn(IWorld world) {
		if (!connections.containsKey(world)) {
			Create.logger.warn(
					"Tried to Access unprepared network space of " + WorldHelper.getDimensionID(world));
			return new HashMap<>();
		}
		return connections.get(world);
	}

}
