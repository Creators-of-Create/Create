package com.simibubi.create;

import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.TypeBlasting;
import com.simibubi.create.content.contraptions.processing.fan.TypeHaunting;
import com.simibubi.create.content.contraptions.processing.fan.TypeSmoking;
import com.simibubi.create.content.contraptions.processing.fan.TypeSplashing;
import com.simibubi.create.content.contraptions.processing.fan.transform.HorseTransform;

public class AllFanProcessingTypes {

	public static final AbstractFanProcessingType SPLASHING = new TypeSplashing(2000, "SPLASHING");
	public static final AbstractFanProcessingType HAUNTING = new TypeHaunting(1000, "HAUNTING");
	public static final AbstractFanProcessingType SMOKING = new TypeSmoking(-1000, "SMOKING");
	public static final AbstractFanProcessingType BLASTING = new TypeBlasting(-2000, "BLASTING");
	public static final HorseTransform HORSE = new HorseTransform();

	public static void register() {
	}
}
