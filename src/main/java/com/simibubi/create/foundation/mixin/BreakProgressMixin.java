package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.BreakProgressHook;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientWorld.class)
public class BreakProgressMixin {
	@Shadow
	@Final
	private WorldRenderer worldRenderer;
	private final ClientWorld self = (ClientWorld) (Object) this;

	@Inject(at = @At("HEAD"), method = "sendBlockBreakProgress")
	private void onBreakProgress(int playerEntityId, BlockPos pos, int progress, CallbackInfo ci) {
		BreakProgressHook.whenBreaking(self, this.worldRenderer, playerEntityId, pos, progress);
	}
}
