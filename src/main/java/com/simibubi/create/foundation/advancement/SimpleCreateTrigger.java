package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleCreateTrigger extends CriterionTriggerBase<SimpleCreateTrigger.Instance> {

	public SimpleCreateTrigger(String id) {
		super(id);
	}

	@Override
	public Instance createInstance(JsonObject json, DeserializationContext context) {
		return new Instance(getId());
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, null);
	}

	public Instance instance() {
		return new Instance(getId());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance(ResourceLocation idIn) {
			super(idIn, EntityPredicate.Composite.ANY);
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
