package com.simibubi.create.foundation.render.backend.instancing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

public class SelectInstance<D extends InstanceData> {

	final List<InstancedModel<D>> models;

	ModelSelector selector;

	private int last = -1;
	@Nullable
	private D current;

	public SelectInstance(ModelSelector selector) {
		this.models = new ArrayList<>();
		this.selector = selector;
	}

	public SelectInstance<D> addModel(InstancedModel<D> model) {
		models.add(model);
		return this;
	}

	public SelectInstance<D> update() {
		int i = selector.modelIndexToShow();

		if (i < 0 || i >= models.size()) {
			if (current != null) {
				current.delete();
				current = null;
			}
		} else if (i != last) {
			if (current != null) current.delete();

			current = models.get(i).createInstance();
		}

		last = i;
		return this;
	}

	public Optional<D> get() {
		return Optional.ofNullable(current);
	}

	public void delete() {
		if (current != null) current.delete();
	}

	public interface ModelSelector {
		int modelIndexToShow();
	}
}
