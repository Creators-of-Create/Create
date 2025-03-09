package com.simibubi.create.content.itemprocessing.specifics.press;

import com.simibubi.create.content.itemprocessing.specifics.ICanProcessInBasin;
import com.simibubi.create.content.itemprocessing.specifics.ICanProcessInWorldItems;
import com.simibubi.create.content.itemprocessing.specifics.ICanProcessItemsOnBelt;
import com.simibubi.create.content.itemprocessing.specifics.ICreateParticles;
import com.simibubi.create.content.kinetics.IHaveKineticSpeed;

/**
 * Valid processing for a mechanical press
 */
public interface PressProcessingSpecifics extends
	IHaveKineticSpeed,
	ICanProcessInWorldItems,
	ICanProcessInBasin,
	ICanProcessItemsOnBelt,
	ICreateParticles {
}
