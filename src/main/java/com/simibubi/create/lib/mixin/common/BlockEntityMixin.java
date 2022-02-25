package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.lib.extensions.BlockEntityExtensions;
import com.simibubi.create.lib.util.MixinHelper;
import com.simibubi.create.lib.util.NBTSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityExtensions, NBTSerializable {
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
