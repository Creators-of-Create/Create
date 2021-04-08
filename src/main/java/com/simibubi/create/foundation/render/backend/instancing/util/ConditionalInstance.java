package com.simibubi.create.foundation.render.backend.instancing.util;

import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

public class ConditionalInstance<D extends InstanceData> {

	final InstancedModel<D> model;
	Condition condition;

	@Nullable
	private D instance;

	public ConditionalInstance(InstancedModel<D> model, Condition condition) {
		this.model = model;
		this.condition = condition;

		update();
	}

	public ConditionalInstance<D> setCondition(Condition condition) {
		this.condition = condition;
		return this;
	}

	public ConditionalInstance<D> update() {
		boolean shouldShow = condition.shouldShow();
		if (shouldShow && instance == null) {
			instance = model.createInstance();
		} else if (!shouldShow && instance != null) {
			instance.delete();
			instance = null;
		}

		return this;
	}

	public Optional<D> get() {
		return Optional.ofNullable(instance);
	}

	public void delete() {
		if (instance != null) instance.delete();
	}

	@FunctionalInterface
	public interface Condition {
		boolean shouldShow();
	}
}
