package com.simibubi.create.foundation.render;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;

public class GroupInstance<D extends AbstractInstance> extends AbstractCollection<D> {

	final Instancer<D> model;
	final List<D> backing;

	public GroupInstance(Instancer<D> model) {
		this.model = model;

		this.backing = new ArrayList<>();
	}

	public GroupInstance(Instancer<D> model, int size) {
		this.model = model;

		this.backing = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			addInstance();
		}
	}

	/**
	 * @param count
	 * @return True if the number of elements changed.
	 */
	public boolean resize(int count) {
		int size = size();
		if (count == size) return false;

		if (count <= 0) {
			clear();
			return size > 0;
		}

		if (count > size) {
			for (int i = size; i < count; i++) {
				addInstance();
			}
		} else {
			List<D> unnecessary = backing.subList(count, size);
			unnecessary.forEach(AbstractInstance::delete);
			unnecessary.clear();
		}

		return true;
	}

	public D addInstance() {
		D instance = model.createInstance();
		backing.add(instance);

		return instance;
	}

	public D get(int index) {
		return backing.get(index);
	}

	@Override
	public Iterator<D> iterator() {
		return backing.iterator();
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public void clear() {
		backing.forEach(AbstractInstance::delete);
		backing.clear();
	}
}
