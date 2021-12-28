package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.block.CustomFrictionBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Boat.class)
public abstract class BoatMixin extends Entity {
	// you can't capture locals in a @ModifyVariable, so we have this
	@Unique
	private BlockState create$state;
	@Unique
	private Level create$world;
	@Unique
	private BlockPos.MutableBlockPos create$pos;
	@Unique
	private Entity create$entity;

	public BoatMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "getGroundFriction()F",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getFriction()F"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void create$getGroundFriction(CallbackInfoReturnable<Float> cir,
									AABB aabb, AABB aabb2, int i, int j, int k,
									int l, int m, int n, VoxelShape shape, float f, int o, BlockPos.MutableBlockPos mutable,
									int p, int q, int r, int s, BlockState blockState) {
		create$state = blockState;
		create$world = level;
		create$pos = mutable;
		create$entity = this;
	}

	@Redirect(method = "getGroundFriction()F", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
	public float create$setFriction(Block instance) {
		if (instance instanceof CustomFrictionBlock custom) {
			return custom.getFriction(create$state, create$world, create$pos, create$entity);
		}
		return instance.getFriction();
	}
}
