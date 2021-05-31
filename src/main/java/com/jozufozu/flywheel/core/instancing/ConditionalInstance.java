package com.jozufozu.flywheel.core.instancing;

import java.util.Optional;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;

public class ConditionalInstance<D extends InstanceData> {

	final Instancer<D> model;
	Condition condition;

	@Nullable
	private D instance;

	public ConditionalInstance(Instancer<D> model, Condition condition) {
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
