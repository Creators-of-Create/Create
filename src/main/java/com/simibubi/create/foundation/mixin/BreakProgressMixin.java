package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.block.BreakProgressHook;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientWorld.class)
public class BreakProgressMixin {
	@Shadow
	@Final
	private WorldRenderer levelRenderer; // levelRenderer
	private final ClientWorld self = (ClientWorld) (Object) this;

	@Inject(at = @At("HEAD"), method = "destroyBlockProgress")
	private void onBreakProgress(int playerEntityId, BlockPos pos, int progress, CallbackInfo ci) {
		BreakProgressHook.whenBreaking(self, this.levelRenderer, playerEntityId, pos, progress);
	}
}
