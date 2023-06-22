package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(StairBlock.class)
public interface StairBlockAccessor {
	@Accessor("stateSupplier")
	void setStateSupplier(Supplier<BlockState> stateSupplier);
}
