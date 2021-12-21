package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.BlockEntityExtensions;
import com.simibubi.create.lib.util.TileEntityHelper;
import com.simibubi.create.lib.util.MixinHelper;
import com.simibubi.create.lib.util.NBTSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityExtensions, NBTSerializable {
	@Unique
	private CompoundTag create$extraCustomData;

	@Override
	public CompoundTag create$getExtraCustomData() {
		if (create$extraCustomData == null) {
			create$extraCustomData = new CompoundTag();
		}
		return create$extraCustomData;
	}

	@Inject(method = "load",
			at = @At("TAIL"))
	public void load(CompoundTag compoundNBT, CallbackInfo ci) {
		if (compoundNBT.contains(TileEntityHelper.EXTRA_DATA_KEY))
			this.create$extraCustomData = compoundNBT.getCompound(TileEntityHelper.EXTRA_DATA_KEY);
	}

	@Inject(method = "saveMetadata",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", ordinal = 2))
	private void saveMetadata(CompoundTag compoundNBT, CallbackInfo ci) {
		if (this.create$extraCustomData != null) {
			compoundNBT.put(TileEntityHelper.EXTRA_DATA_KEY, this.create$extraCustomData.copy());
		}
	}

	@Override
	public CompoundTag create$serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		MixinHelper.<BlockEntity>cast(this).load(nbt);
		return nbt;
	}

	@Override
	public void create$deserializeNBT(CompoundTag nbt) {
		create$deserializeNBT(null, nbt);
	}

	public void create$deserializeNBT(BlockState state, CompoundTag nbt) {
		MixinHelper.<BlockEntity>cast(this).load(nbt);
	}
}
