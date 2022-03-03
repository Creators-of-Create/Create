package com.simibubi.create;

import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.TypeBlasting;
import com.simibubi.create.content.contraptions.processing.fan.TypeHaunting;
import com.simibubi.create.content.contraptions.processing.fan.TypeSmoking;
import com.simibubi.create.content.contraptions.processing.fan.TypeSplashing;
import com.simibubi.create.content.contraptions.processing.fan.transform.HorseTransform;

import net.minecraft.resources.ResourceLocation;

public class AllFanProcessingTypes {

	public static final AbstractFanProcessingType SPLASHING = new TypeSplashing(2000, new ResourceLocation(Create.ID, "splashing"));
	public static final AbstractFanProcessingType HAUNTING = new TypeHaunting(1000, new ResourceLocation(Create.ID, "haunting"));
	public static final AbstractFanProcessingType SMOKING = new TypeSmoking(-1000, new ResourceLocation(Create.ID, "smoking"));
	public static final AbstractFanProcessingType BLASTING = new TypeBlasting(-2000, new ResourceLocation(Create.ID, "blasting"));
	public static final HorseTransform HORSE = new HorseTransform();

	public static void register() {
	}
}
