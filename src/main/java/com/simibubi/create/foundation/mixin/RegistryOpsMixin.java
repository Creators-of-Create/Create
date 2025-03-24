package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.codec.ResourceLocationAwareOps;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Implements(@Interface(iface = ResourceLocationAwareOps.class, prefix = "create$", unique = true))
@Mixin(RegistryOps.class)
public class RegistryOpsMixin {
	@Unique
	private @Nullable ResourceLocation create$resourceLocation;

	public @Nullable ResourceLocation create$getResourceLocation() {
		return create$resourceLocation;
	}

	public void create$setResourceLocation(@Nullable ResourceLocation location) {
		create$resourceLocation = location;
	}
}
