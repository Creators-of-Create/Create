package com.simibubi.create.lib.mixin.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.lib.event.RegisterShadersCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void registerShaders(ResourceManager manager, CallbackInfo ci, List<Program> list, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderRegistry) {
		ArrayList<Pair<ShaderInstance, Consumer<ShaderInstance>>> moddedShaders = new ArrayList<>();
		try {
			RegisterShadersCallback.EVENT.invoker().registerShaders(moddedShaders, manager);
		} catch (IOException e) {
			moddedShaders.forEach(shader -> shader.getFirst().close());
			throw new RuntimeException("could not reload modded shaders", e);
		}
		shaderRegistry.addAll(moddedShaders);
	}
}
