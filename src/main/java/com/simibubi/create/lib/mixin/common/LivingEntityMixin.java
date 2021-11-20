package com.simibubi.create.lib.mixin.common;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.LivingEntityEvents;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.item.EntitySwingListenerItem;
import com.simibubi.create.lib.item.EquipmentItem;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	@Shadow
	protected Player lastHurtByPlayer;

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand interactionHand);

	@Unique
	private DamageSource create$currentDamageSource;

	public LivingEntityMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void create$spawnDropsHEAD(DamageSource source, CallbackInfo ci) {
		create$currentDamageSource = source;
		((EntityExtensions) this).create$captureDrops(new ArrayList<>());
	}

	@ModifyVariable(method = "dropAllDeathLoot",
			at = @At(value = "STORE", ordinal = 0))
	private int create$spawnDropsBODY(int j) {
		int modifiedLevel = LivingEntityEvents.LOOTING_LEVEL.invoker().modifyLootingLevel(create$currentDamageSource);
		if (modifiedLevel != 0) {
			return modifiedLevel;
		} else {
			return EnchantmentHelper.getMobLooting((LivingEntity) create$currentDamageSource.getEntity());
		}
	}

	@ModifyVariable(method = "dropAllDeathLoot",
			at = @At(value = "STORE", ordinal = 1))
	private int create$spawnDropsBODY2(int j) {
		return LivingEntityEvents.LOOTING_LEVEL.invoker().modifyLootingLevel(create$currentDamageSource);
	}

	@Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
	private void create$spawnDropsTAIL(DamageSource source, CallbackInfo ci) {
		Collection<ItemEntity> drops = ((EntityExtensions) this).create$captureDrops(null);
		if (!LivingEntityEvents.DROPS.invoker().onLivingEntityDrops(source, drops))
			drops.forEach(e -> level.addFreshEntity(e));
	}

	@Environment(EnvType.CLIENT)
	@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
	private void create$swingHand(InteractionHand hand, boolean bl, CallbackInfo ci) {
		ItemStack stack = getItemInHand(hand);
		if (!stack.isEmpty() && stack.getItem() instanceof EntitySwingListenerItem && ((EntitySwingListenerItem) stack.getItem())
				.onEntitySwing(stack, (LivingEntity) (Object) this)) ci.cancel();
	}

	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void create$tick(CallbackInfo ci) {
		LivingEntityEvents.TICK.invoker().onLivingEntityTick((LivingEntity) (Object) this);
	}

	@ModifyVariable(method = "knockback", at = @At("STORE"), ordinal = 0)
	private double create$takeKnockback(double f) {
		if (lastHurtByPlayer != null)
			return LivingEntityEvents.KNOCKBACK_STRENGTH.invoker().onLivingEntityTakeKnockback(f, lastHurtByPlayer);

		return f;
	}

	@ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
	private float create$onHurt(float amount, DamageSource source, float amount2) {
		return LivingEntityEvents.HURT.invoker().onHurt(source, amount);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", shift = At.Shift.BEFORE),
			locals = LocalCapture.CAPTURE_FAILHARD,
			method = "checkFallDamage", cancellable = true)
	protected void create$updateFallState(double d, boolean bl, BlockState blockState, BlockPos blockPos, CallbackInfo ci,
										  float f, double e, int i) {
		if (((BlockStateExtensions) blockState).create$addLandingEffects((ServerLevel) level, blockPos, blockState, MixinHelper.cast(this), i)) {
			super.checkFallDamage(d, bl, blockState, blockPos);
			ci.cancel();
		}
	}

	// TODO Make this less :concern: when fabric's mixin fork updates
	@ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"),
			method = "travel",
			index = 7)
	public float create$setSlipperiness(float t) {
		return ((BlockStateExtensions) MixinHelper.<LivingEntity>cast(this).level.getBlockState(getBlockPosBelowThatAffectsMyMovement()))
				.create$getSlipperiness(MixinHelper.<LivingEntity>cast(this).level, getBlockPosBelowThatAffectsMyMovement(), MixinHelper.<LivingEntity>cast(this));
	}

	//Moved from MobMixin
	@Inject(at = @At("HEAD"), method = "getEquipmentSlotForItem", cancellable = true)
	private static void create$getSlotForItemStack(ItemStack itemStack, CallbackInfoReturnable<EquipmentSlot> cir) {
		if (itemStack.getItem() instanceof EquipmentItem equipment) {
			cir.setReturnValue(equipment.getEquipmentSlot(itemStack));
		}
	}
}
