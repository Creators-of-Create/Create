package com.simibubi.create.content.logistics.packagerLink;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RequestPromiseQueue {
	private Map<Item, List<RequestPromise>> promisesByItem;
	private Runnable onChanged;

	public RequestPromiseQueue(Runnable onChanged) {
		promisesByItem = new IdentityHashMap<>();
		this.onChanged = onChanged;
	}

	public void add(RequestPromise promise) {
		promisesByItem.computeIfAbsent(promise.promisedStack.stack.getItem(), $ -> new LinkedList<>())
			.add(promise);
		onChanged.run();
	}

	public void setOnChanged(Runnable onChanged) {
		this.onChanged = onChanged;
	}

	public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
		int promised = 0;
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return promised;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise promise = iterator.next();
			if (!ItemStack.isSameItemSameComponents(promise.promisedStack.stack, stack))
				continue;
			if (expiryTime != -1 && promise.ticksExisted >= expiryTime) {
				iterator.remove();
				onChanged.run();
				continue;
			}

			promised += promise.promisedStack.count;
		}
		return promised;
	}

	public void forceClear(ItemStack stack) {
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise promise = iterator.next();
			if (!ItemStack.isSameItemSameComponents(promise.promisedStack.stack, stack))
				continue;
			iterator.remove();
			onChanged.run();
		}

		if (list.isEmpty())
			promisesByItem.remove(stack.getItem());
	}

	public void itemEnteredSystem(ItemStack stack, int amount) {
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise requestPromise = iterator.next();
			if (!ItemStack.isSameItemSameComponents(requestPromise.promisedStack.stack, stack))
				continue;

			int toSubtract = Math.min(amount, requestPromise.promisedStack.count);
			amount -= toSubtract;
			requestPromise.promisedStack.count -= toSubtract;

			if (requestPromise.promisedStack.count <= 0) {
				iterator.remove();
				onChanged.run();
			}
			if (amount <= 0)
				break;
		}

		if (list.isEmpty())
			promisesByItem.remove(stack.getItem());
	}

	public List<RequestPromise> flatten(boolean sorted) {
		List<RequestPromise> all = new ArrayList<>();
		promisesByItem.forEach((key, list) -> all.addAll(list));
		if (sorted)
			all.sort(RequestPromise.ageComparator());
		return all;
	}

	public CompoundTag write(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.put("List", CatnipCodecUtils.encode(Codec.list(RequestPromise.CODEC), registries, flatten(false)).orElseThrow());
		return tag;
	}

	public static RequestPromiseQueue read(CompoundTag tag, HolderLookup.Provider registries, Runnable onChanged) {
		RequestPromiseQueue queue = new RequestPromiseQueue(onChanged);
		List<RequestPromise> promises = CatnipCodecUtils.decode(Codec.list(RequestPromise.CODEC), registries, tag.get("List")).orElse(List.of());
		for (RequestPromise promise : promises) {
			queue.add(promise);
		}
		return queue;
	}

	public void tick() {
		promisesByItem.forEach((key, list) -> list.forEach(RequestPromise::tick)); // delete old entries?
	}

	public boolean isEmpty() {
		return promisesByItem.isEmpty();
	}
}
