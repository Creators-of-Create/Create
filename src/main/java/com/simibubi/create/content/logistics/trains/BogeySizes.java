package com.simibubi.create.content.logistics.trains;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class BogeySizes {
	private static final Collection<BogeySize> BOGEY_SIZES = new HashSet<>();
	public static final BogeySize SMALL = new BogeySize(Create.ID, "small", 6.5f / 16f);
	public static final BogeySize LARGE = new BogeySize(Create.ID, "large", 12.5f / 16f);

	static {
		BOGEY_SIZES.add(SMALL);
		BOGEY_SIZES.add(LARGE);
	}

	public static BogeySize addSize(String modId, String name, float size) {
		ResourceLocation location = new ResourceLocation(modId, name);
		return addSize(location, size);
	}

	public static BogeySize addSize(ResourceLocation location, float size) {
		BogeySize customSize = new BogeySize(location, size);
		BOGEY_SIZES.add(customSize);
		return customSize;
	}

	public static List<BogeySize> getAllSizesSmallToLarge() {
		return BOGEY_SIZES.stream()
				.sorted(Comparator.comparing(BogeySize::wheelRadius))
				.collect(Collectors.toList());
	}

	public static List<BogeySize> getAllSizesLargeToSmall() {
		List<BogeySize> sizes = getAllSizesSmallToLarge();
		Collections.reverse(sizes);
		return sizes;
	}

	public static int count() {
		return BOGEY_SIZES.size();
	}

	public record BogeySize(ResourceLocation location, float wheelRadius) {
		public BogeySize(String modId, String name, float wheelRadius) {
			this(new ResourceLocation(modId, name), wheelRadius);
		}

		public BogeySize increment() {
			List<BogeySize> values = getAllSizesSmallToLarge();
			int ordinal = values.indexOf(this);
			return values.get((ordinal + 1) % values.size());
		}

		public boolean is(BogeySize size) {
			return size.location == this.location;
		}
	}

	public static void init() {

	}
}
