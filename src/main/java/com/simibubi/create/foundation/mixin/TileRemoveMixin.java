package com.simibubi.create.foundation.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.CreateClient;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Mixin(TileEntity.class)
public class TileRemoveMixin {

	@Shadow
	@Nullable
	protected World world;

	@Inject(at = @At("TAIL"), method = "remove")
	private void onRemove(CallbackInfo ci) {
		if (world instanceof ClientWorld)
			CreateClient.kineticRenderer.get(this.world)
				.remove((TileEntity) (Object) this);
	}
}
