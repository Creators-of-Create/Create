package com.simibubi.create.lib.mixin.client;

import com.mojang.blaze3d.shaders.Program;

import net.minecraft.client.renderer.ShaderInstance;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.packs.resources.ResourceProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ShaderInstance.class, priority = 500)
public class ShaderInstanceMixin {

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V", ordinal = 0))
	private String fixId(String path) {
		String name = path.split("/")[2].replace(".json", "");
		if (!name.contains(":")) {
			return path;
		}
		ResourceLocation split = new ResourceLocation(name);
		return split.getNamespace() + ":shaders/core/" + split.getPath() + ".json";
	}

	@ModifyVariable(method = "getOrCreate", at = @At("STORE"), ordinal = 1)
	private static String fixPath(String path, final ResourceProvider factory, Program.Type type, String name) {
		if (!name.contains(":")) {
			return path;
		}
		ResourceLocation split = new ResourceLocation(name);
		return split.getNamespace() + ":shaders/core/" + split.getPath() + type.getExtension();
	}
}
