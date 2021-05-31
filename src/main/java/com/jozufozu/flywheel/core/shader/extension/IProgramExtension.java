package com.jozufozu.flywheel.core.shader.extension;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.spec.SpecMetaRegistry;
import com.mojang.serialization.Codec;

import net.minecraft.util.ResourceLocation;

/**
 * A factory interface for creating {@link IExtensionInstance}s. These are what end up being passed in
 * during shader program construction.
 */
public interface IProgramExtension {

	Codec<IProgramExtension> CODEC = ResourceLocation.CODEC.xmap(SpecMetaRegistry::getExtension, IProgramExtension::getID);

	/**
	 * Construct the extension, binding any necessary information using the provided {@link GlProgram}.
	 *
	 * @param program The program being extended.
	 * @return An extension object, possibly initialized using the program.
	 */
	IExtensionInstance create(GlProgram program);

	ResourceLocation getID();
}
