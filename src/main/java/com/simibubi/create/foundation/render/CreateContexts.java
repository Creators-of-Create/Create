package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.Templates;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.render.ContraptionProgram;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateContexts {
	private static final ResourceLocation CONTRAPTION = Create.asResource("context/contraption");

	public static ProgramCompiler<ContraptionProgram> CWORLD;
	public static ProgramCompiler<ContraptionProgram> STRUCTURE;

	public static void flwInit(GatherContextEvent event) {
		GameStateRegistry.register(RainbowDebugStateProvider.INSTANCE);
        FileResolution header = Resolver.INSTANCE.get(ResourceUtil.subPath(CONTRAPTION, ".glsl"));

		CWORLD = ProgramCompiler.create(Templates.INSTANCING, ContraptionProgram::new, header);
		STRUCTURE = ProgramCompiler.create(Templates.ONE_SHOT, ContraptionProgram::new, header);
	}

}
