package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.lib.block.MinecartPassHandlerBlock;
import com.simibubi.create.lib.extensions.AbstractMinecartExtensions;
import com.simibubi.create.lib.util.MinecartAndRailUtil;
import com.simibubi.create.lib.util.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity implements AbstractMinecartExtensions {

	public MinecartController create$controller = null;
	public boolean create$canUseRail = true;

	private AbstractMinecartMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	@Shadow
	protected abstract double getMaxSpeed();

	@Shadow
	public abstract AbstractMinecart.Type getMinecartType();

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
	public void create$abstractMinecartEntity(EntityType<?> entityType, Level world, CallbackInfo ci) {
		create$controller = new MinecartController(MixinHelper.cast(this));
		CapabilityMinecartController.attach(MixinHelper.cast(this));
	}

	@Inject(method = "moveAlongTrack", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 4))
	protected void create$moveAlongTrack(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (blockState.getBlock() instanceof MinecartPassHandlerBlock handler) {
			handler.onMinecartPass(blockState, level, blockPos, MixinHelper.cast(this));
		}
	}

	@Override
	public void create$moveMinecartOnRail(BlockPos pos) {
		double d24 = isVehicle() ? 0.75D : 1.0D;
		double d25 = getMaxSpeed(); // getMaxSpeed instead of getMaxSpeedWithRail *should* be fine after intense pain looking at Forge patches
		Vec3 vec3d1 = getDeltaMovement();
		move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
	}

	@Override
	public boolean create$canUseRail() {
		return create$canUseRail;
	}

	@Override
	public BlockPos create$getCurrentRailPos() {
		BlockPos pos = new BlockPos(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));
		if (level.getBlockState(pos.below()).is(BlockTags.RAILS)) {
			pos = pos.below();
		}

		return pos;
	}

	@Override
	public MinecartController create$getController() {
		return create$controller;
	}
}
