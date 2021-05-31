package com.jozufozu.flywheel.core.shader;

import java.util.function.Function;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.extension.IExtensionInstance;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.core.shader.extension.UnitExtensionInstance;

import net.minecraft.util.ResourceLocation;

public enum WorldFog implements IProgramExtension {
	NONE("none", UnitExtensionInstance::new),
	LINEAR("linear", FogMode.Linear::new),
	EXP2("exp2", FogMode.Exp2::new),
	;

	private final ResourceLocation id;
	private final String name;
	private final Function<GlProgram, IExtensionInstance> fogFactory;

	WorldFog(String name, Function<GlProgram, IExtensionInstance> fogFactory) {
		this.id = new ResourceLocation(Flywheel.ID, "fog_" + name);
		this.name = name;
		this.fogFactory = fogFactory;
	}

	public String getName() {
		return name;
	}

	@Override
	public IExtensionInstance create(GlProgram program) {
		return fogFactory.apply(program);
	}

	@Override
	public ResourceLocation getID() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}
}
