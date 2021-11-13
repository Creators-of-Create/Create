package com.simibubi.create.lib.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.OnModelRegistryCallback;
import com.simibubi.create.lib.utility.SpecialModelUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> bakedCache;

	@Shadow
	public abstract UnbakedModel getModel(ResourceLocation p_209597_1_);

	private void create$addModelToCache(ResourceLocation p_217843_1_) {
		UnbakedModel iunbakedmodel = this.getModel(p_217843_1_);
		this.unbakedCache.put(p_217843_1_, iunbakedmodel);
		this.bakedCache.put(p_217843_1_, iunbakedmodel);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift = At.Shift.BEFORE),
			method = "<init>")
	public void create$onModelRegistry(ResourceManager iResourceManager, BlockColors blockColors, ProfilerFiller iProfiler, int i, CallbackInfo ci) {
		OnModelRegistryCallback.EVENT.invoker().onModelRegistry();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 4),
			method = "<init>")
	public void create$registerSpecialModels(ResourceManager iResourceManager, BlockColors blockColors, ProfilerFiller iProfiler, int i, CallbackInfo ci) {
		for (ResourceLocation location : SpecialModelUtil.specialModels) {
			create$addModelToCache(location);
		}
	}
}
