package com.simibubi.create.foundation.advancement;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.Create;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllTriggers {

	private static final List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static SimpleCreateTrigger addSimple(String id) {
		return add(new SimpleCreateTrigger(id));
	}

	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.add(instance);
		return instance;
	}

	public static void register() {
		triggers.forEach(trigger -> {
			Registry.register(BuiltInRegistries.TRIGGER_TYPES, trigger.getId(), trigger);
		});
	}

}
