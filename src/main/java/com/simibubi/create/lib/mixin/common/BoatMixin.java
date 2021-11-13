package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Boat.class)
public abstract class BoatMixin {
	// you can't capture locals in a @ModifyVariable, so we have this
	@Unique
	BlockState create$state;
	@Unique
	Level create$world;
	@Unique
	BlockPos.MutableBlockPos create$pos;
	@Unique
	Entity create$entity;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"),
			locals = LocalCapture.CAPTURE_FAILHARD,
			method = "getGroundFriction()F")
	public void create$getBoatGlide(CallbackInfoReturnable<Float> cir,
									AABB AABB, AABB AABB2, int i, int j, int k,
									int l, int m, int n, VoxelShape shape, float f, int o, BlockPos.MutableBlockPos mutable,
									int p, int q, int r, int s, BlockState blockState) {
		create$state = blockState;
		create$world = MixinHelper.<Boat>cast(this).level;
		create$pos = mutable;
		create$entity = MixinHelper.<Boat>cast(this);
	}

	@ModifyVariable(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/Block;getFriction()F"),
			method = "getGroundFriction()F")
	public float create$setSlipperiness(float f) {
		return ((BlockStateExtensions) create$state).create$getSlipperiness(create$world, create$pos, create$entity);
	}
}
