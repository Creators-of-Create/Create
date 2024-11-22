package com.simibubi.create;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import com.simibubi.create.content.trains.bogey.BogeyStyle.SizeRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyVisual;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.resources.ResourceLocation;

public class AllBogeyStyles {
	public static final Map<ResourceLocation, BogeyStyle> BOGEY_STYLES = new HashMap<>();
	public static final Map<ResourceLocation, Map<ResourceLocation, BogeyStyle>> CYCLE_GROUPS = new HashMap<>();
	private static final Map<ResourceLocation, BogeyStyle> EMPTY_GROUP = Collections.emptyMap();

	public static final ResourceLocation STANDARD_CYCLE_GROUP = Create.asResource("standard");

	public static final BogeyStyle STANDARD =
		builder("standard", STANDARD_CYCLE_GROUP).displayName(Components.translatable("create.bogey.style.standard"))
			.size(BogeySizes.SMALL, AllBlocks.SMALL_BOGEY, () -> new SizeRenderer(new StandardBogeyRenderer.Small(), StandardBogeyVisual.Small::new))
			.size(BogeySizes.LARGE, AllBlocks.LARGE_BOGEY, () -> new SizeRenderer(new StandardBogeyRenderer.Large(), StandardBogeyVisual.Large::new))
			.build();

	public static Map<ResourceLocation, BogeyStyle> getCycleGroup(ResourceLocation cycleGroup) {
		return CYCLE_GROUPS.getOrDefault(cycleGroup, EMPTY_GROUP);
	}

	private static BogeyStyle.Builder builder(String name, ResourceLocation cycleGroup) {
		return new BogeyStyle.Builder(Create.asResource(name), cycleGroup);
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
