package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.core.ContraptionContext;
import com.jozufozu.flywheel.backend.core.EffectsContext;
import com.jozufozu.flywheel.backend.core.WorldContext;
import com.jozufozu.flywheel.backend.effects.EffectsHandler;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.IFlywheelWorld;
import com.simibubi.create.foundation.config.AllConfigs;

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
	public static EffectsHandler effects;

	public static Matrix4f projectionMatrix = new Matrix4f();

	public static GLCapabilities capabilities;
	public static GlCompat compat;

	private static boolean instancingAvailable;
	private static boolean enabled;

	static final Map<ResourceLocation, ShaderContext<?>> contexts = new HashMap<>();
	static final Map<ResourceLocation, ProgramSpec> specRegistry = new HashMap<>();

	static {
		register(WorldContext.INSTANCE);
		register(ContraptionContext.INSTANCE);
		register(EffectsContext.INSTANCE);
	}

	public Backend() {
		throw new IllegalStateException();
	}

	/**
	 * Register a shader program.
	 */
	public static ProgramSpec register(ProgramSpec spec) {
		ResourceLocation name = spec.name;
		if (specRegistry.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		specRegistry.put(name, spec);
		return spec;
	}

	/**
	 * Register a shader context.
	 */
	public static ShaderContext<?> register(ShaderContext<?> spec) {
		ResourceLocation name = spec.getRoot();
		if (contexts.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		contexts.put(name, spec);
		return spec;
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

		if (enabled) {
			if (effects != null) effects.delete();
			effects = new EffectsHandler();
		}
	}
}
