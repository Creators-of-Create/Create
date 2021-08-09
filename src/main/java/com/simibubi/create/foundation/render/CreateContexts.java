package com.simibubi.create.foundation.render;

import java.util.stream.Stream;

import org.lwjgl.opengl.GL46;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.FileResolution;
import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.SpecMetaRegistry;
import com.jozufozu.flywheel.backend.pipeline.IShaderPipeline;
import com.jozufozu.flywheel.backend.pipeline.ITemplate;
import com.jozufozu.flywheel.backend.pipeline.InstancingTemplate;
import com.jozufozu.flywheel.backend.pipeline.OneShotTemplate;
import com.jozufozu.flywheel.backend.pipeline.WorldShaderPipeline;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateContexts {
	private static final ResourceLocation CONTRAPTION = new ResourceLocation("create", "context/contraption");

	public static WorldContext<ContraptionProgram> CWORLD;
	public static WorldContext<ContraptionProgram> STRUCTURE;

	public static void flwInit(GatherContextEvent event) {
		Backend backend = event.getBackend();

		SpecMetaRegistry.register(RainbowDebugStateProvider.INSTANCE);

		CWORLD = backend.register(contraptionContext(backend, InstancingTemplate.INSTANCE));
		STRUCTURE = backend.register(contraptionContext(backend, OneShotTemplate.INSTANCE)
				.withSpecStream(() -> Stream.of(AllProgramSpecs.STRUCTURE)));
	}

	private static WorldContext<ContraptionProgram> contraptionContext(Backend backend, ITemplate template) {

		FileResolution header = backend.sources.resolveFile(ResourceUtil.subPath(CONTRAPTION, ".glsl"));

		IShaderPipeline<ContraptionProgram> worldPipeline = new WorldShaderPipeline<>(backend.sources, ContraptionProgram::new, template, header);

		return new WorldContext<>(backend, worldPipeline)
				.withName(CONTRAPTION);
	}
}
