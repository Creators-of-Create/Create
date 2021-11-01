package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.block.BreakProgressHook;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

@Mixin(ClientLevel.class)
public class BreakProgressMixin {
	@Shadow
	@Final
	private LevelRenderer levelRenderer; // levelRenderer
	private final ClientLevel self = (ClientLevel) (Object) this;

	@Inject(at = @At("HEAD"), method = "destroyBlockProgress")
	private void onBreakProgress(int playerEntityId, BlockPos pos, int progress, CallbackInfo ci) {
		BreakProgressHook.whenBreaking(self, this.levelRenderer, playerEntityId, pos, progress);
	}
}
