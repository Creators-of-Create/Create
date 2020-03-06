package com.simibubi.create.foundation.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SandpaperUseTrigger extends CriterionTriggerBase<SandpaperUseTrigger.Instance> {

	public SandpaperUseTrigger(String id) {
		super(id);
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		return new SandpaperUseTrigger.Instance(getId(), ItemPredicate.deserialize(json.get("target")),ItemPredicate.deserialize(json.get("result")));
	}

	public void trigger(ServerPlayerEntity player, ItemStack target, ItemStack result){
		trigger(player, Arrays.asList(() -> target, () -> result));
	}

	public static class Instance extends CriterionTriggerBase.Instance {
		private final ItemPredicate target;
		private final ItemPredicate result;

		public Instance(ResourceLocation idIn, ItemPredicate target, ItemPredicate result) {
			super(idIn);
			this.target = target;
			this.result = result;

		}

		@Override
		protected boolean test(List<Supplier<Object>> suppliers) {
			return this.target.test((ItemStack) suppliers.get(0).get()) &&
					this.result.test((ItemStack) suppliers.get(1).get());
		}
	}
}
