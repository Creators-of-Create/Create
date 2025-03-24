package com.simibubi.create.foundation.codec;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public interface ResourceLocationAwareOps {
	@Nullable ResourceLocation getResourceLocation();

	void setResourceLocation(@Nullable ResourceLocation location);
}
