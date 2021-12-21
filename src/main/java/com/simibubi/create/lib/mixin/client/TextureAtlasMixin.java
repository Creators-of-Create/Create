package com.simibubi.create.lib.mixin.client;

import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.event.OnTextureStitchCallback;
import com.simibubi.create.lib.util.MixinHelper;
import com.simibubi.create.lib.util.TextureStitchUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.AFTER, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD,
			method = "prepareToStitch")
	public void create$stitch(ResourceManager iResourceManager, Stream<ResourceLocation> stream, ProfilerFiller iProfiler, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir, Set<ResourceLocation> set) {
		OnTextureStitchCallback.PRE.invoker().onModelRegistry(new TextureStitchUtil(MixinHelper.cast(this), set));
	}

	@Inject(method = "reload", at = @At("TAIL"))
	public void create$stitchReload(TextureAtlas.Preparations preparations, CallbackInfo ci) {
		OnTextureStitchCallback.POST.invoker().onModelRegistry(new TextureStitchUtil(MixinHelper.cast(this), null));
	}
}
