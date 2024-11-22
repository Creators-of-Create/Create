package com.simibubi.create.content.trains.bogey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceLocation;

public final class BogeySizes {
	private static final Map<ResourceLocation, BogeySize> BOGEY_SIZES = new HashMap<>();
	private static final List<BogeySize> SORTED_INCREASING = new ArrayList<>();
	private static final List<BogeySize> SORTED_DECREASING = new ArrayList<>();
	@UnmodifiableView
	private static final Map<ResourceLocation, BogeySize> BOGEY_SIZES_VIEW = Collections.unmodifiableMap(BOGEY_SIZES);
	@UnmodifiableView
	private static final List<BogeySize> SORTED_INCREASING_VIEW = Collections.unmodifiableList(SORTED_INCREASING);
	@UnmodifiableView
	private static final List<BogeySize> SORTED_DECREASING_VIEW = Collections.unmodifiableList(SORTED_DECREASING);

	public static final BogeySize SMALL = new BogeySize(Create.asResource("small"), 6.5f / 16f);
	public static final BogeySize LARGE = new BogeySize(Create.asResource("large"), 12.5f / 16f);

	static {
		register(SMALL);
		register(LARGE);
	}

	private BogeySizes() {
	}

	public static void register(BogeySize size) {
		ResourceLocation id = size.id();
		if (BOGEY_SIZES.containsKey(id)) {
			throw new IllegalArgumentException();
		}
		BOGEY_SIZES.put(id, size);

		SORTED_INCREASING.add(size);
		SORTED_DECREASING.add(size);
		SORTED_INCREASING.sort(Comparator.comparing(BogeySize::wheelRadius));
		SORTED_DECREASING.sort(Comparator.comparing(BogeySize::wheelRadius).reversed());
	}

	@UnmodifiableView
	public static Map<ResourceLocation, BogeySize> all() {
		return BOGEY_SIZES_VIEW;
	}

	@UnmodifiableView
	public static List<BogeySize> allSortedIncreasing() {
		return SORTED_INCREASING_VIEW;
	}

	@UnmodifiableView
	public static List<BogeySize> allSortedDecreasing() {
		return SORTED_DECREASING_VIEW;
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public record BogeySize(ResourceLocation id, float wheelRadius) {
		public BogeySize nextBySize() {
			List<BogeySize> values = allSortedIncreasing();
			int ordinal = values.indexOf(this);
			return values.get((ordinal + 1) % values.size());
		}
	}
}
