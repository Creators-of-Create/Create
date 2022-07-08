package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.block.render.DestroyProgressRenderingHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.client.RenderProperties;

@Mixin(ClientLevel.class)
public class DestroyProgressMixin {
	@Shadow
	@Final
	private LevelRenderer levelRenderer;

	@Inject(at = @At("HEAD"), method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", cancellable = true)
	private void onDestroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
		ClientLevel self = (ClientLevel) (Object) this;
		BlockState state = self.getBlockState(pos);
		IBlockRenderProperties properties = RenderProperties.get(state);
		if (properties instanceof DestroyProgressRenderingHandler handler) {
			if (handler.renderDestroyProgress(self, levelRenderer, breakerId, pos, progress, state)) {
				ci.cancel();
			}
		} else if (progress == -1)
			levelRenderer.destroyBlockProgress(pos.hashCode(), pos, -1);
	}
}
