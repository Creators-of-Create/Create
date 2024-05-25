package com.simibubi.create.foundation.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;

public class SelectInstance<D extends Instance> {

	final List<Instancer<D>> models;

	ModelSelector selector;

	private int last = -1;
	@Nullable
	private D current;

	public SelectInstance(ModelSelector selector) {
		this.models = new ArrayList<>();
		this.selector = selector;
	}

	public SelectInstance<D> addModel(Instancer<D> model) {
		models.add(model);
		return this;
	}

	public SelectInstance<D> update() {
		int i = selector.modelIndexToShow();

		if (i < 0 || i >= models.size()) {
			if (current != null) {
				current.handle().setDeleted();
				current = null;
			}
		} else if (i != last) {
			if (current != null) current.handle().setDeleted();

			current = models.get(i)
					.createInstance();
		}

		last = i;
		return this;
	}

	public Optional<D> get() {
		return Optional.ofNullable(current);
	}

	public void delete() {
		if (current != null) current.handle().setDeleted();
	}

	public void forEach(Consumer<Instance> consumer) {
		if (current != null) {
			consumer.accept(current);
		}
	}

    public interface ModelSelector {
		int modelIndexToShow();
	}
}
