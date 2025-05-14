package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ConcretePowderBlock.class)
public interface ConcretePowderBlockAccessor {
	@Accessor("concrete")
	BlockState create$getConcrete();
}
