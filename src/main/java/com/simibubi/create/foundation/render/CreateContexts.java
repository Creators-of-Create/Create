package com.simibubi.create.foundation.render;

import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.SpecMetaRegistry;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ModelTemplate;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.foundation.render.effects.EffectsContext;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateContexts {
	private static final ResourceLocation CONTRAPTION = new ResourceLocation("create", "context/contraption");

	public static EffectsContext EFFECTS;
	public static WorldContext<ContraptionProgram> CWORLD;
	public static WorldContext<ContraptionProgram> STRUCTURE;

	@SubscribeEvent
	public static void flwInit(GatherContextEvent event) {
		Backend backend = event.getBackend();

		SpecMetaRegistry.register(RainbowDebugStateProvider.INSTANCE);

		EFFECTS = backend.register(new EffectsContext(backend));
		CWORLD = backend.register(contraptionContext(backend));
		STRUCTURE = backend.register(contraptionContext(backend)
				.withSpecStream(() -> Stream.of(AllProgramSpecs.STRUCTURE))
				.withTemplateFactory(ModelTemplate::new));
	}

	private static WorldContext<ContraptionProgram> contraptionContext(Backend backend) {
		return new WorldContext<>(backend, ContraptionProgram::new)
				.withName(CONTRAPTION)
				.withBuiltin(ShaderType.FRAGMENT, CONTRAPTION, "/builtin.frag")
				.withBuiltin(ShaderType.VERTEX, CONTRAPTION, "/builtin.vert");
	}
}
