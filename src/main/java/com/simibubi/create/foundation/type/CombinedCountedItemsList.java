package com.simibubi.create.foundation.type;

import java.util.HashMap;
import java.util.Map;

public class CombinedCountedItemsList<T> {

	protected Map<T, CountedItemsList> lists = new HashMap<>();
	protected CountedItemsList combined = new CountedItemsList();
	boolean combinedListDirty = true;

	public void add(T key, CountedItemsList list) {
		lists.put(key, list);
		combinedListDirty = true;
	}

	public void remove(T key) {
		lists.remove(key);
		combinedListDirty = true;
	}
	
	public void clear() {
		lists.clear();
		combinedListDirty = true;
	}

	public CountedItemsList get() {
		if (combinedListDirty) {
			combined = new CountedItemsList();
			lists.values().forEach(list -> list.getFlattenedList().forEach(combined::add));
			combinedListDirty = false;
		}
		return combined;
	}

}
