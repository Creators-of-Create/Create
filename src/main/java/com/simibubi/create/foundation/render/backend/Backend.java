package com.simibubi.create.foundation.render.backend;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.foundation.render.backend.gl.GlFog;
import com.simibubi.create.foundation.render.backend.gl.shader.*;
import com.simibubi.create.foundation.render.backend.gl.versioned.GlFeatureCompat;
import com.simibubi.create.foundation.render.backend.instancing.IFlywheelWorld;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

public class Backend {
	public static final Boolean SHADER_DEBUG_OUTPUT = true;

	public static final Logger log = LogManager.getLogger(Backend.class);
	public static GLCapabilities capabilities;
	public static GlFeatureCompat compat;

	private static boolean instancingAvailable;
	private static boolean enabled;

	static final Map<ResourceLocation, ProgramSpec<?>> registry = new HashMap<>();
	static final Map<ProgramSpec<?>, ProgramGroup<?>> programs = new HashMap<>();

	public Backend() {
		throw new IllegalStateException();
	}

	/**
	 * Register a shader program. TODO: replace with forge registry?
	 */
	public static <P extends GlProgram, S extends ProgramSpec<P>> S register(S spec) {
		ResourceLocation name = spec.name;
		if (registry.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		registry.put(name, spec);
		return spec;
	}

	@SuppressWarnings("unchecked")
	public static <P extends GlProgram, S extends ProgramSpec<P>> P getProgram(S spec) {
		return (P) programs.get(spec).get(GlFog.getFogMode());
	}

	public static boolean isFlywheelWorld(World world) {
		return world == Minecraft.getInstance().world || (world instanceof IFlywheelWorld && ((IFlywheelWorld) world).supportsFlywheel());
	}

	public static boolean available() {
		return canUseVBOs();
	}

	public static boolean canUseInstancing() {
		return enabled && instancingAvailable;
	}

	public static boolean canUseVBOs() {
		return enabled && gl20();
	}

	public static boolean gl33() {
		return capabilities.OpenGL33;
	}

	public static boolean gl20() {
		return capabilities.OpenGL20;
	}

	public static void init() {
		// Can be null when running datagenerators due to the unfortunate time we call this
		Minecraft mc = Minecraft.getInstance();
		if (mc == null) return;

		IResourceManager manager = mc.getResourceManager();

		if (manager instanceof IReloadableResourceManager) {
			ISelectiveResourceReloadListener listener = ShaderLoader::onResourceManagerReload;
			((IReloadableResourceManager) manager).addReloadListener(listener);
		}
	}

	public static void refresh() {
		capabilities = GL.createCapabilities();

		compat = new GlFeatureCompat(capabilities);

		instancingAvailable = compat.vertexArrayObjectsSupported() &&
				compat.drawInstancedSupported() &&
				compat.instancedArraysSupported();

		enabled = AllConfigs.CLIENT.experimentalRendering.get() && !OptifineHandler.usingShaders();
	}
}
