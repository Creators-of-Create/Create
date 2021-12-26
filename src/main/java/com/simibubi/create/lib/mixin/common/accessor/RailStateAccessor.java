package com.simibubi.create.lib.mixin.common.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RailState;

@Mixin(RailState.class)
public interface RailStateAccessor {
	@Accessor("pos")
	BlockPos create$getPos();

	@Invoker("canConnectTo")
	boolean create$canConnectTo(RailState railState);

	@Invoker("removeSoftConnections")
	void create$removeSoftConnections();
}
