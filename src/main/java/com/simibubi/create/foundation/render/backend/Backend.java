package com.simibubi.create.foundation.render.backend;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.render.backend.gl.shader.GlProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.GlShader;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderType;
import com.simibubi.create.foundation.render.backend.gl.versioned.GlVersioned;
import com.simibubi.create.foundation.render.backend.gl.versioned.MapBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Backend {
    public static final Logger log = LogManager.getLogger(Backend.class);
    public static final FloatBuffer FLOAT_BUFFER = MemoryUtil.memAllocFloat(1); // TODO: these leak 80 bytes of memory per program launch
    public static final FloatBuffer VEC4_BUFFER = MemoryUtil.memAllocFloat(4);
    public static final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);

    private static final Map<ResourceLocation, ProgramSpec<?>> registry = new HashMap<>();
    private static final Map<ProgramSpec<?>, GlProgram> programs = new HashMap<>();

    private static boolean enabled;

    public static GLCapabilities capabilities;
    private static MapBuffer mapBuffer;

    public Backend() {
        throw new IllegalStateException();
    }

    public static void mapBuffer(int target, int offset, int length, Consumer<ByteBuffer> upload) {
        mapBuffer.mapBuffer(target, offset, length, upload);
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
        return (P) programs.get(spec);
    }

    /**
     * Get the most compatible version of a specific OpenGL feature by iterating over enum constants in order.
     *
     * @param clazz The class of the versioning enum.
     * @param <V> The type of the versioning enum.
     * @return The first defined enum variant to return true.
     */
    public static <V extends Enum<V> & GlVersioned> V getLatest(Class<V> clazz) {
        return getLatest(clazz, capabilities);
    }

    /**
     * Get the most compatible version of a specific OpenGL feature by iterating over enum constants in order.
     *
     * @param clazz The class of the versioning enum.
     * @param caps The current system's supported features.
     * @param <V> The type of the versioning enum.
     * @return The first defined enum variant to return true.
     */
    public static <V extends Enum<V> & GlVersioned> V getLatest(Class<V> clazz, GLCapabilities caps) {
        V[] constants = clazz.getEnumConstants();
        V last = constants[constants.length - 1];
        if (!last.supported(caps)) {
            throw new IllegalStateException("");
        }

        return Arrays.stream(constants).filter(it -> it.supported(caps)).findFirst().orElse(last);
    }

    public static boolean canUseInstancing() {
        return enabled && gl33();
    }

    public static boolean canUseVBOs() {
        return enabled && gl20();
    }

    public static boolean available() {
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
            mapBuffer = getLatest(MapBuffer.class);

            OptifineHandler.refresh();
            refresh();

            if (gl20()) {

                programs.values().forEach(GlProgram::delete);
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
        GlShader vert = null;
        GlShader frag = null;
        try {
            vert = loadShader(manager, programSpec.getVert(), ShaderType.VERTEX, programSpec.defines);
            frag = loadShader(manager, programSpec.getFrag(), ShaderType.FRAGMENT, programSpec.defines);

            GlProgram.Builder builder = GlProgram.builder(programSpec.name).attachShader(vert).attachShader(frag);

            programSpec.attributes.forEach(builder::addAttribute);

            P program = builder.build(programSpec.factory);

            programs.put(programSpec, program);

            log.info("Loaded program {}", programSpec.name);
        } catch (IOException ex) {
            log.error("Failed to load program {}", programSpec.name, ex);
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
