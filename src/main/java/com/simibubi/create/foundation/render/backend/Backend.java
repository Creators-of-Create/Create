package com.simibubi.create.foundation.render.backend;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.render.backend.gl.GlFog;
import com.simibubi.create.foundation.render.backend.gl.shader.GlProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.IMultiProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.backend.gl.versioned.GlCompat;
import com.simibubi.create.foundation.render.backend.instancing.IFlywheelWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

public class Backend {
	public static final Logger log = LogManager.getLogger(Backend.class);

	public static final ShaderLoader shaderLoader = new ShaderLoader();

	public static Matrix4f projectionMatrix = new Matrix4f();

	public static GLCapabilities capabilities;
	public static GlCompat compat;

	private static boolean instancingAvailable;
	private static boolean enabled;

	static final Map<ResourceLocation, ProgramSpec<?>> registry = new HashMap<>();
	static final Map<ProgramSpec<?>, IMultiProgram<?>> programs = new HashMap<>();

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
		return (P) programs.get(spec).get();
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
			ISelectiveResourceReloadListener listener = shaderLoader::onResourceManagerReload;
			((IReloadableResourceManager) manager).addReloadListener(listener);
		}
	}

	public static void refresh() {
		capabilities = GL.createCapabilities();

		compat = new GlCompat(capabilities);

		instancingAvailable = compat.vertexArrayObjectsSupported() &&
				compat.drawInstancedSupported() &&
				compat.instancedArraysSupported();

		enabled = AllConfigs.CLIENT.experimentalRendering.get() && !OptifineHandler.usingShaders();
	}
}
