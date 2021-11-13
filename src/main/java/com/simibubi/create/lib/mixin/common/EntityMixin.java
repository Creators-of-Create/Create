package com.simibubi.create.lib.mixin.common;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.entity.CustomPosHandlingEntity;
import com.simibubi.create.lib.event.EntityEyeHeightCallback;
import com.simibubi.create.lib.event.StartRidingCallback;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.helper.EntityHelper;
import com.simibubi.create.lib.utility.ListenerProvider;
import com.simibubi.create.lib.utility.MixinHelper;
import com.simibubi.create.lib.utility.NBTSerializable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtensions, NBTSerializable {
	@Unique
	private static final Logger CREATE$LOGGER = LogManager.getLogger();
	@Shadow
	public Level level;
	@Shadow
	private float eyeHeight;

	@Shadow
	protected abstract void readAdditionalSaveData(CompoundTag compoundTag);

	@Unique
	private CompoundTag create$extraCustomData;
	@Unique
	private Collection<ItemEntity> create$captureDrops = null;

	@Inject(at = @At("TAIL"), method = "<init>")
	public void create$entityInit(EntityType<?> entityType, Level world, CallbackInfo ci) {
		int newEyeHeight = EntityEyeHeightCallback.EVENT.invoker().onEntitySize((Entity) (Object) this);
		if (newEyeHeight != -1)
			eyeHeight = newEyeHeight;
	}

	@Inject(at = @At("HEAD"), method = "setPosRaw", cancellable = true)
	public final void setPosRaw(double d, double e, double f, CallbackInfo ci) {
		if (this instanceof CustomPosHandlingEntity entity) {
			entity.setPosRawOverride(d, e, f);
			ci.cancel();
		}
	}

	// CAPTURE DROPS

	@Inject(locals = LocalCapture.CAPTURE_FAILHARD,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setDefaultPickUpDelay()V", shift = At.Shift.AFTER),
			method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", cancellable = true)
	public void create$spawnAtLocation(ItemStack stack, float f, CallbackInfoReturnable<ItemEntity> cir, ItemEntity itemEntity) {
		if (create$captureDrops != null) create$captureDrops.add(itemEntity);
		else cir.cancel();
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops() {
		return create$captureDrops;
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops(Collection<ItemEntity> value) {
		Collection<ItemEntity> ret = create$captureDrops;
		create$captureDrops = value;
		return ret;
	}

	// EXTRA CUSTOM DATA

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"),
			method = "saveWithoutId")
	public void create$beforeWriteCustomData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (create$extraCustomData != null && !create$extraCustomData.isEmpty()) {
			CREATE$LOGGER.debug("Create: writing custom data to entity [{}]", MixinHelper.<Entity>cast(this).toString());
			tag.put(EntityHelper.EXTRA_DATA_KEY, create$extraCustomData);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"), method = "load")
	public void create$beforeReadCustomData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(EntityHelper.EXTRA_DATA_KEY)) {
			create$extraCustomData = tag.getCompound(EntityHelper.EXTRA_DATA_KEY);
		}
	}

	// RUNNING EFFECTS

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILHARD,
			method = "spawnSprintParticle", cancellable = true)
	public void create$spawnSprintParticle(CallbackInfo ci, int i, int j, int k, BlockPos blockPos) {
		if (((BlockStateExtensions) level.getBlockState(blockPos)).create$addRunningEffects(level, blockPos, MixinHelper.cast(this))) {
			ci.cancel();
		}
	}

	//

	@Inject(method = "discard()V", at = @At("HEAD"))
	public void create$discard(CallbackInfo ci) {
		if (this instanceof ListenerProvider) {
			((ListenerProvider) this).invalidate();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canAddPassenger(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.BEFORE),
			method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", cancellable = true)
	public void create$startRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		if (StartRidingCallback.EVENT.invoker().onStartRiding(MixinHelper.cast(this), entity) == InteractionResult.FAIL) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	@Override
	public CompoundTag create$getExtraCustomData() {
		if (create$extraCustomData == null) {
			create$extraCustomData = new CompoundTag();
		}
		return create$extraCustomData;
	}

	@Unique
	@Override
	public CompoundTag create$serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		String id = EntityHelper.getEntityString(MixinHelper.cast(this));

		if (id != null) {
			nbt.putString("id", id);
		}

		return nbt;
	}

	@Unique
	@Override
	public void create$deserializeNBT(CompoundTag nbt) {
		readAdditionalSaveData(nbt);
	}
}
