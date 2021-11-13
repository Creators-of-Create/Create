package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.math.Vector3f;

@Mixin(Vector3f.class)
public interface Vector3fAccessor {
	@Accessor("x")
	void create$x(float x);

	@Accessor("y")
	void create$y(float y);

	@Accessor("z")
	void create$z(float z);
}
