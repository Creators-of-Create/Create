package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Backend {
	public static final Logger log = LogManager.getLogger(Backend.class);

	public static final ShaderSources SHADER_SOURCES = new ShaderSources();

	public static GLCapabilities capabilities;
	public static GlCompat compat;

	private static Matrix4f projectionMatrix = new Matrix4f();
	private static boolean instancedArrays;
	private static boolean enabled;

	static final Map<ResourceLocation, MaterialSpec<?>> materialRegistry = new HashMap<>();
	static final List<ShaderContext<?>> contexts = new ArrayList<>();
	static final Map<ResourceLocation, ProgramSpec> programSpecRegistry = new HashMap<>();

	static {
		register(WorldContext.INSTANCE);
		register(WorldContext.CRUMBLING);
	}

	public Backend() {
		throw new IllegalStateException();
	}

	/**
	 * Get a string describing the Flywheel backend. When there are eventually multiple backends
	 * (Meshlet, MDI, GL31 Draw Instanced are planned), this will name which one is in use.
	 */
	public static String getBackendDescriptor() {
		if (canUseInstancing()) {
			return "GL33 Instanced Arrays";
		}

		if (canUseVBOs()) {
			return "VBOs";
		}

		return "Disabled";
	}

	/**
	 * Register a shader program.
	 */
	public static ProgramSpec register(ProgramSpec spec) {
		ResourceLocation name = spec.name;
		if (programSpecRegistry.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		programSpecRegistry.put(name, spec);
		return spec;
	}

	/**
	 * Register a shader context.
	 */
	public static <P extends GlProgram> ShaderContext<P> register(ShaderContext<P> spec) {
		contexts.add(spec);
		return spec;
	}

	/**
	 * Register an instancing material.
	 */
	public static <D extends InstanceData> MaterialSpec<D> register(MaterialSpec<D> spec) {
		ResourceLocation name = spec.name;
		if (materialRegistry.containsKey(name)) {
			throw new IllegalStateException("Material spec '" + name + "' already registered.");
		}
		materialRegistry.put(name, spec);
		return spec;
	}

	public static ProgramSpec getSpec(ResourceLocation name) {
		return programSpecRegistry.get(name);
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) worlds that don't specifically support it.
	 */
	public static boolean isFlywheelWorld(IWorld world) {
		return (world instanceof IFlywheelWorld && ((IFlywheelWorld) world).supportsFlywheel()) || world == Minecraft.getInstance().world;
	}

	public static boolean available() {
		return canUseVBOs();
	}

	public static boolean canUseInstancing() {
		return enabled && instancedArrays;
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
			((IReloadableResourceManager) manager).addReloadListener(SHADER_SOURCES);
		}

		OptifineHandler.init();
	}

	public static void refresh() {
		capabilities = GL.createCapabilities();

		compat = new GlCompat(capabilities);

		instancedArrays = compat.vertexArrayObjectsSupported() &&
				compat.drawInstancedSupported() &&
				compat.instancedArraysSupported();

		enabled = AllConfigs.CLIENT.experimentalRendering.get() && !OptifineHandler.usingShaders();
	}

	public static void reloadWorldRenderers() {
		RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
	}

	public static boolean canUseInstancing(World world) {
		return canUseInstancing() && isFlywheelWorld(world);
	}

	public static Collection<MaterialSpec<?>> allMaterials() {
		return materialRegistry.values();
	}

	public static Collection<ProgramSpec> allPrograms() {
		return programSpecRegistry.values();
	}

	public static Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public static void setProjectionMatrix(Matrix4f projectionMatrix) {
		Backend.projectionMatrix = projectionMatrix;
	}
}
