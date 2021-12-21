package com.simibubi.create.lib.util;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public class SpecialModelUtil {
	public static Set<ResourceLocation> specialModels = new HashSet<>();
	public static void addSpecialModel(ResourceLocation location) {
		specialModels.add(location);
	}
}
