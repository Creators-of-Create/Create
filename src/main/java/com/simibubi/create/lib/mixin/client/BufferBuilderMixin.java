package com.simibubi.create.lib.mixin.client;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Pair;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {
	@Shadow
	private ByteBuffer buffer;

	@Inject(method = "popNextBuffer", at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;clear()Ljava/nio/ByteBuffer;"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void bufferOrder(CallbackInfoReturnable<Pair<BufferBuilder.DrawState, ByteBuffer>> cir, BufferBuilder.DrawState bufferbuilder$drawstate, ByteBuffer byteBuffer) {
		byteBuffer.order(this.buffer.order()); // Fix incorrect byte order
	}
}
