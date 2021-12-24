package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(VoxelShape.class)
public interface VoxelShapeAccessor {
	@Accessor("shape")
	DiscreteVoxelShape create$getShape();
}
