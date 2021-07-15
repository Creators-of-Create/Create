package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleTrigger extends CriterionTriggerBase<SimpleTrigger.Instance> implements ITriggerable {

	public SimpleTrigger(String id) {
		super(id);
	}

	@Override
	public Instance createInstance(JsonObject json, ConditionArrayParser context) {
		return new Instance(getId());
	}

	public void trigger(ServerPlayerEntity player) {
		super.trigger(player, null);
	}

	public Instance instance() {
		return new Instance(getId());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance(ResourceLocation idIn) {
			super(idIn, EntityPredicate.AndPredicate.ANY); // FIXME: Is this right?
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
