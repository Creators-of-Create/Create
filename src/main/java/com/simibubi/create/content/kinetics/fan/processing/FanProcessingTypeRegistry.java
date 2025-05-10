package com.simibubi.create.content.kinetics.fan.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.UnmodifiableView;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class FanProcessingTypeRegistry {
	private static final List<FanProcessingType> SORTED_TYPES = new ReferenceArrayList<>();
	@UnmodifiableView
	public static final List<FanProcessingType> SORTED_TYPES_VIEW = Collections.unmodifiableList(SORTED_TYPES);

	@Internal
	public static void init() {
		SORTED_TYPES.clear();
		CreateBuiltInRegistries.FAN_PROCESSING_TYPE.forEach(SORTED_TYPES::add);
		SORTED_TYPES.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
	}
}
