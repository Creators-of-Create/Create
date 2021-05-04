package com.jozufozu.flywheel.backend;

import net.minecraft.util.ResourceLocation;

public class ResourceUtil {

	public static ResourceLocation subPath(ResourceLocation root, String subPath) {
		return new ResourceLocation(root.getNamespace(), root.getPath() + subPath);
	}
}
