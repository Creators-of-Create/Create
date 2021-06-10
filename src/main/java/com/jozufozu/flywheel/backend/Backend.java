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

import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Backend {
	public static final Logger log = LogManager.getLogger(Backend.class);

	protected static final Backend INSTANCE = new Backend();

	public static Backend getInstance() {
		return INSTANCE;
	}

	public final Minecraft minecraft;
	public ShaderSources sources;

	public GLCapabilities capabilities;
	public GlCompat compat;

	private Matrix4f projectionMatrix = new Matrix4f();
	private boolean instancedArrays;
	private boolean enabled;

	private final List<IShaderContext<?>> contexts = new ArrayList<>();
	private final Map<ResourceLocation, MaterialSpec<?>> materialRegistry = new HashMap<>();
	private final Map<ResourceLocation, ProgramSpec> programSpecRegistry = new HashMap<>();

	protected Backend() {
		// Can be null when running datagenerators due to the unfortunate time we call this
		minecraft = Minecraft.getInstance();
		if (minecraft == null) return;

		sources = new ShaderSources(this);

		OptifineHandler.init();
	}

	void clearContexts() {
		SpecMetaRegistry.clear();
		contexts.forEach(IShaderContext::delete);
		materialRegistry.clear();
	}

	/**
	 * Get a string describing the Flywheel backend. When there are eventually multiple backends
	 * (Meshlet, MDI, GL31 Draw Instanced are planned), this will name which one is in use.
	 */
	public String getBackendDescriptor() {
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
	public ProgramSpec register(ProgramSpec spec) {
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
	public <C extends ShaderContext<?>> C register(C spec) {
		contexts.add(spec);
		return spec;
	}

	/**
	 * Register an instancing material.
	 */
	public <D extends InstanceData> MaterialSpec<D> register(MaterialSpec<D> spec) {
		ResourceLocation name = spec.name;
		if (materialRegistry.containsKey(name)) {
			throw new IllegalStateException("Material spec '" + name + "' already registered.");
		}
		materialRegistry.put(name, spec);
		return spec;
	}

	public ProgramSpec getSpec(ResourceLocation name) {
		return programSpecRegistry.get(name);
	}

	public boolean available() {
		return canUseVBOs();
	}

	public boolean canUseInstancing() {
		return enabled && instancedArrays;
	}

	public boolean canUseVBOs() {
		return enabled && gl20();
	}

	public boolean gl33() {
		return capabilities.OpenGL33;
	}

	public boolean gl20() {
		return capabilities.OpenGL20;
	}

	public void refresh() {
		OptifineHandler.refresh();
		capabilities = GL.createCapabilities();

		compat = new GlCompat(capabilities);

		instancedArrays = compat.vertexArrayObjectsSupported() &&
				compat.drawInstancedSupported() &&
				compat.instancedArraysSupported();

		enabled = AllConfigs.CLIENT.experimentalRendering.get() && !OptifineHandler.usingShaders();
	}

	public boolean canUseInstancing(World world) {
		return canUseInstancing() && isFlywheelWorld(world);
	}

	public Collection<MaterialSpec<?>> allMaterials() {
		return materialRegistry.values();
	}

	public Collection<ProgramSpec> allPrograms() {
		return programSpecRegistry.values();
	}

	public Collection<IShaderContext<?>> allContexts() {
		return contexts;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) worlds that don't specifically support it.
	 */
	public static boolean isFlywheelWorld(IWorld world) {
		return (world instanceof IFlywheelWorld && ((IFlywheelWorld) world).supportsFlywheel()) || world == Minecraft.getInstance().world;
	}

	public static void reloadWorldRenderers() {
		RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
	}
}
