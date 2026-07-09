package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

@ParametersAreNonnullByDefault
public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {

	public CriterionTriggerBase(String id) {
		this.id = Create.asResource(id);
	}

	private final Identifier id;

	public Identifier getId() {
		return id;
	}

	protected void trigger(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
		trigger(player, instance -> instance.test(suppliers));
	}

	public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
		protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
	}

}
