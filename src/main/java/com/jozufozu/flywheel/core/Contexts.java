package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.SpecMetaRegistry;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.WorldFog;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.gamestate.FogStateProvider;
import com.jozufozu.flywheel.core.shader.gamestate.NormalDebugStateProvider;
import com.jozufozu.flywheel.event.GatherContextEvent;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Contexts {

	public static WorldContext<WorldProgram> WORLD;
	public static WorldContext<CrumblingProgram> CRUMBLING;

	@SubscribeEvent
	public static void flwInit(GatherContextEvent event) {
		Backend backend = event.getBackend();

		SpecMetaRegistry.register(FogStateProvider.INSTANCE);
		SpecMetaRegistry.register(NormalDebugStateProvider.INSTANCE);

		SpecMetaRegistry.register(WorldFog.LINEAR);
		SpecMetaRegistry.register(WorldFog.EXP2);

		CRUMBLING = backend.register(new WorldContext<>(backend, CrumblingProgram::new)
				.withName(Names.CRUMBLING)
				.withBuiltin(ShaderType.FRAGMENT, Names.CRUMBLING, "/builtin.frag")
				.withBuiltin(ShaderType.VERTEX, Names.CRUMBLING, "/builtin.vert"));

		WORLD = backend.register(new WorldContext<>(backend, WorldProgram::new)
				.withName(Names.WORLD)
				.withBuiltin(ShaderType.FRAGMENT, Names.WORLD, "/builtin.frag")
				.withBuiltin(ShaderType.VERTEX, Names.WORLD, "/builtin.vert"));
	}

	public static class Names {
		public static final ResourceLocation CRUMBLING = new ResourceLocation(Flywheel.ID, "context/crumbling");
		public static final ResourceLocation WORLD = new ResourceLocation(Flywheel.ID, "context/world");
	}
}
