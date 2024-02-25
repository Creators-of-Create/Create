package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.lib.context.SimpleContextShader;
import com.simibubi.create.Create;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllContextShaders {
	public static final ContextShader CONTRAPTION = ContextShader.REGISTRY.registerAndGet(new SimpleContextShader(
			Create.asResource("context/contraption.vert"),
			Create.asResource("context/contraption.frag")
	));

	private AllContextShaders() {

	}

	public static void init() {
		// register statics
	}
}
