package com.simibubi.create.foundation.render.backend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.simibubi.create.foundation.render.backend.gl.GlFog;
import com.simibubi.create.foundation.render.backend.gl.GlFogMode;
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
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

public class Backend {
    public static final Boolean SHADER_DEBUG_OUTPUT = true;

    public static final Logger log = LogManager.getLogger(Backend.class);
    public static final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);

    private static final Map<ResourceLocation, ProgramSpec<?>> registry = new HashMap<>();
    private static final Map<ProgramSpec<?>, ProgramGroup<?>> programs = new HashMap<>();

    private static boolean enabled;

    public static GLCapabilities capabilities;
    public static GlFeatureCompat compat;

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
        return enabled &&
                compat.vertexArrayObjectsSupported() &&
                compat.drawInstancedSupported() &&
                compat.instancedArraysSupported();
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
            ISelectiveResourceReloadListener listener = Backend::onResourceManagerReload;
            ((IReloadableResourceManager) manager).addReloadListener(listener);
        }
    }

    private static void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate) {
        if (predicate.test(VanillaResourceType.SHADERS)) {
            capabilities = GL.createCapabilities();
            compat = new GlFeatureCompat(capabilities);

            OptifineHandler.refresh();
            refresh();

            if (gl20()) {

                programs.values().forEach(ProgramGroup::delete);
                programs.clear();
                for (ProgramSpec<?> shader : registry.values()) {
                    loadProgram(manager, shader);
                }
            }
        }
    }

    public static void refresh() {
        enabled = AllConfigs.CLIENT.experimentalRendering.get() && !OptifineHandler.usingShaders();
    }

    private static <P extends GlProgram, S extends ProgramSpec<P>> void loadProgram(IResourceManager manager, S programSpec) {
        try {
            Map<GlFogMode, P> programGroup = new EnumMap<>(GlFogMode.class);

            for (GlFogMode fogMode : GlFogMode.values()) {
                programGroup.put(fogMode, loadProgram(manager, programSpec, fogMode));
            }

            programs.put(programSpec, new ProgramGroup<>(programGroup));

            log.info("Loaded program {}", programSpec.name);
        } catch (IOException ex) {
            log.error("Failed to load program {}", programSpec.name, ex);
            return;
        }
    }

    private static <P extends GlProgram, S extends ProgramSpec<P>> P loadProgram(IResourceManager manager, S programSpec, GlFogMode fogMode) throws IOException {
        GlShader vert = null;
        GlShader frag = null;
        try {
            ShaderConstants defines = new ShaderConstants(programSpec.defines);

            defines.defineAll(fogMode.getDefines());

            vert = loadShader(manager, programSpec.getVert(), ShaderType.VERTEX, defines);
            frag = loadShader(manager, programSpec.getFrag(), ShaderType.FRAGMENT, defines);

            GlProgram.Builder builder = GlProgram.builder(programSpec.name, fogMode).attachShader(vert).attachShader(frag);

            programSpec.attributes.forEach(builder::addAttribute);

            return builder.build(programSpec.factory);

        } finally {
            if (vert != null) vert.delete();
            if (frag != null) frag.delete();
        }
    }

    private static GlShader loadShader(IResourceManager manager, ResourceLocation name, ShaderType type, GlShader.PreProcessor preProcessor) throws IOException {
        try (InputStream is = new BufferedInputStream(manager.getResource(name).getInputStream())) {
            String source = TextureUtil.func_225687_b_(is);

            if (source == null) {
                throw new IOException("Could not load program " + name);
            } else {
                return new GlShader(type, name, source, preProcessor);
            }
        }
    }
}
