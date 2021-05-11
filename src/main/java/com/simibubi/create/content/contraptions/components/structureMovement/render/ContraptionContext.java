package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.core.WorldContext;
import com.jozufozu.flywheel.backend.gl.shader.FogSensitiveProgram;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.minecraft.util.ResourceLocation;

public class ContraptionContext extends WorldContext<ContraptionProgram> {

	public static final ContraptionContext INSTANCE = new ContraptionContext();

	public ContraptionContext() {
		super(new ResourceLocation("create", "context/contraption"), new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new));
	}

	@Override
	public void load(ShaderLoader loader) {
		super.load(loader);

		loadProgramFromSpec(loader, AllProgramSpecs.STRUCTURE);
	}
}
