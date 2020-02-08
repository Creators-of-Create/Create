package com.simibubi.create.foundation.advancement;

import net.minecraft.advancements.CriteriaTriggers;

import java.util.LinkedList;
import java.util.List;

public class AllCriterionTriggers {

	private static List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static SandpaperUseTrigger SANDPAPER_USE = add(new SandpaperUseTrigger("sandpaper_use"));
	public static NoArgumentTrigger DEPLOYER_BOOP = add(new NoArgumentTrigger("deployer"));
	public static NoArgumentTrigger ABSORBED_LIGHT = add(new NoArgumentTrigger("light_absorbed"));
	public static NoArgumentTrigger SPEED_READ = add(new NoArgumentTrigger("speed_read"));

	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.add(instance);
		return instance;
	}

	public static void register(){
		triggers.forEach(CriteriaTriggers::register);
	}


}
