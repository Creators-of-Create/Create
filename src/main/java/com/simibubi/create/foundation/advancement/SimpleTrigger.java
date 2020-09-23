package com.simibubi.create.foundation.advancement;

import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class SimpleTrigger extends CriterionTriggerBase<SimpleTrigger.Instance> implements ITriggerable {

	public SimpleTrigger(String id) {
		super(id);
	}

	@Override
	public Instance conditionsFromJson(JsonObject json, ConditionArrayParser context) {
		return new Instance(getId());
	}

	public void trigger(ServerPlayerEntity player){
		super.trigger(player, null);
	}
	
	public Instance instance() {
		return new Instance(getId());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance(ResourceLocation idIn) {
			super(idIn);
		}

		@Override
		protected boolean test(List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
