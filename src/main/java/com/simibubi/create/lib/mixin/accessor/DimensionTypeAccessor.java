package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.dimension.DimensionType;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
	@Accessor
	static DimensionType getDEFAULT_OVERWORLD() {
		throw new UnsupportedOperationException();
	}
}
