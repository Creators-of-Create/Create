package com.simibubi.create.foundation.render;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;

public class ConditionalInstance<D extends AbstractInstance> {

	final Instancer<D> model;
	ICondition condition;

	Consumer<D> setupFunc;

	@Nullable
	private D instance;

	public ConditionalInstance(Instancer<D> model) {
		this.model = model;
		this.condition = () -> true;
	}

	public ConditionalInstance<D> withSetupFunc(Consumer<D> setupFunc) {
		this.setupFunc = setupFunc;
		return this;
	}

	public ConditionalInstance<D> withCondition(ICondition condition) {
		this.condition = condition;
		return this;
	}

	public ConditionalInstance<D> update() {
		boolean shouldShow = condition.shouldShow();
		if (shouldShow && instance == null) {
			instance = model.createInstance();
			if (setupFunc != null) setupFunc.accept(instance);
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
	public interface ICondition {
		boolean shouldShow();
	}
}
