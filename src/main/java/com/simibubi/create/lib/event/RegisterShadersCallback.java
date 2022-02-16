package com.simibubi.create.lib.event;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface RegisterShadersCallback {
	Event<RegisterShadersCallback> EVENT = EventFactory.createArrayBacked(RegisterShadersCallback.class, callbacks -> (shaderRegistry, resourceManager) -> {
		for (RegisterShadersCallback event : callbacks)
			event.registerShaders(shaderRegistry, resourceManager);
	});

	void registerShaders(List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderRegistry, ResourceManager resourceManager) throws IOException;
}
