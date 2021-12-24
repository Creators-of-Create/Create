package com.simibubi.create.lib.mixin.accessor;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.BiMap;

import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;

@Mixin(HoneycombItem.class)
public interface HoneycombItemAccessor {
	@Mutable
	@Accessor
	static void setWAXABLES(Supplier<BiMap<Block, Block>> WAXABLES) {
		throw new UnsupportedOperationException();
	}
}
