package com.simibubi.create.lib.mixin.accessor;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(WoodType.class)
public interface WoodTypeAccessor {
	@Accessor("VALUES")
	static Set<WoodType> create$VALUES() {
		throw new AssertionError();
	}
}
