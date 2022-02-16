package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.blaze3d.shaders.Program;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

@Environment(EnvType.CLIENT)
@Mixin(value = ShaderInstance.class, priority = 500)
public class ShaderInstanceMixin {
	@Shadow
	@Final
	private String name;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V", ordinal = 0))
	private String fixId(String path) {
		if (!path.contains(":")) {
			return path;
		}
		ResourceLocation split = new ResourceLocation(name);
		return split.getNamespace() + ":shaders/core/" + split.getPath() + ".json";
	}

	@ModifyVariable(method = "getOrCreate", at = @At("STORE"), ordinal = 1)
	private static String fixPath(String path, ResourceProvider resourceProvider, Program.Type programType, String name) {
		if (!name.contains(":")) {
			return path;
		}
		ResourceLocation split = new ResourceLocation(name);
		return split.getNamespace() + ":shaders/core/" + split.getPath() + programType.getExtension();
	}
}
