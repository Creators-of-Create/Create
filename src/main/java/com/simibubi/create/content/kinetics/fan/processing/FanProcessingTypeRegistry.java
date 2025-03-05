package com.simibubi.create.content.kinetics.fan.processing;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class FanProcessingTypeRegistry {
	private static List<FanProcessingType> sortedTypes = null;
	@UnmodifiableView
	private static List<FanProcessingType> sortedTypesView = null;

	@UnmodifiableView
	public static List<FanProcessingType> getSortedTypesView() {
		if (sortedTypes == null || sortedTypesView == null) {
			sortedTypes = new ReferenceArrayList<>();

			CreateBuiltInRegistries.FAN_PROCESSING_TYPE.forEach(sortedTypes::add);
			sortedTypes.sort((t1, t2) -> t2.getPriority() - t1.getPriority());

			sortedTypesView = Collections.unmodifiableList(sortedTypes);
		}

		return sortedTypesView;
	}
}
