package com.jozufozu.flywheel.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.core.BasicInstancedTileRenderer;
import com.jozufozu.flywheel.backend.core.ContraptionContext;
import com.jozufozu.flywheel.backend.core.EffectsContext;
import com.jozufozu.flywheel.backend.core.WorldContext;
import com.jozufozu.flywheel.backend.effects.EffectsHandler;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.IFlywheelWorld;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
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
	public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);

	private static boolean instancingAvailable;
	private static boolean enabled;

	static Map<ResourceLocation, MaterialSpec<?>> materialRegistry = new HashMap<>();
	static final Map<ResourceLocation, ShaderContext<?>> contexts = new HashMap<>();
	static final Map<ResourceLocation, ProgramSpec> programSpecRegistry = new HashMap<>();

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
		ResourceLocation name = spec.getRoot();
		if (contexts.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		contexts.put(name, spec);
		return spec;
	}

	/**
	 * Register an instancing material.
	 */
	public static <M extends InstancedModel<?>> MaterialSpec<M> register(MaterialSpec<M> spec) {
		ResourceLocation name = spec.name;
		if (materialRegistry.containsKey(name)) {
			throw new IllegalStateException("Material spec '" + name + "' already registered.");
		}
		materialRegistry.put(name, spec);
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

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		BasicInstancedTileRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);

		Entity renderViewEntity = mc.renderViewEntity;
		kineticRenderer.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());

		ConcurrentHashMap.KeySetView<TileEntity, Boolean> map = queuedUpdates.get(world);
		map
				.forEach(te -> {
					map.remove(te);

					kineticRenderer.update(te);
				});
	}

	public static void renderLayer(RenderType layer, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
		if (!canUseInstancing()) return;

		ClientWorld world = Minecraft.getInstance().world;
		BasicInstancedTileRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);

		layer.startDrawing();

		kineticRenderer.render(layer, viewProjection, cameraX, cameraY, cameraZ);

		layer.endDrawing();
	}

	public static void enqueueUpdate(TileEntity te) {
		queuedUpdates.get(te.getWorld()).add(te);
	}

	public static void reloadWorldRenderers() {
		RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
	}

	public static boolean canUseInstancing(World world) {
		return canUseInstancing() && isFlywheelWorld(world);
	}

	/**
	 * TODO: Remove in favor of separate debug programs specified by the SpecLoader/IMultiProgram
	 */
	@Deprecated
	public static int getDebugMode() {
		return KineticDebugger.isActive() ? 1 : 0;
	}

	public static Collection<MaterialSpec<?>> allMaterials() {
		return materialRegistry.values();
	}

	public static Collection<ProgramSpec> allPrograms() {
		return programSpecRegistry.values();
	}
}
