package com.simibubi.create.foundation.render.gl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.render.gl.BasicProgram;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderHelper {

    public static final Logger log = LogManager.getLogger(ShaderHelper.class);

    public static final FloatBuffer FLOAT_BUFFER = MemoryUtil.memAllocFloat(1); // TODO: these leak 80 bytes of memory per program launch
    public static final FloatBuffer VEC3_BUFFER = MemoryUtil.memAllocFloat(3);
    public static final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);

    private static final Map<ResourceLocation, ProgramSpec<?>> REGISTRY = new HashMap<>();
    private static final Map<ProgramSpec<?>, GlProgram> PROGRAMS = new HashMap<>();

    public static <P extends GlProgram, S extends ProgramSpec<P>> S register(S spec) {
        ResourceLocation name = spec.name;
        if (REGISTRY.containsKey(name)) {
            throw new IllegalStateException("Program spec '" + name + "' already registered.");
        }
        REGISTRY.put(name, spec);
        return spec;
    }

    public static void initShaders() {
        // Can be null when running datagenerators due to the unfortunate time we call this
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager() instanceof IReloadableResourceManager) {
            ISelectiveResourceReloadListener listener = (manager, predicate) -> {
                if (predicate.test(VanillaResourceType.SHADERS)) {
                    PROGRAMS.values().forEach(GlProgram::delete);
                    PROGRAMS.clear();
                    for (ProgramSpec<?> shader : REGISTRY.values()) {
                        loadProgram(manager, shader);
                    }
                }
            };
            ((IReloadableResourceManager) mc.getResourceManager()).addReloadListener(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public static <P extends GlProgram, S extends ProgramSpec<P>> P getProgram(S spec) {
        return (P) PROGRAMS.get(spec);
    }

    public static void releaseShader() {
        GL20.glUseProgram(0);
    }

    private static <P extends GlProgram, S extends ProgramSpec<P>> void loadProgram(IResourceManager manager, S programSpec) {
        GlShader vert = null;
        GlShader frag = null;
        try {
            vert = loadShader(manager, programSpec.getVert(), ShaderType.VERTEX, programSpec.defines);
            frag = loadShader(manager, programSpec.getFrag(), ShaderType.FRAGMENT, programSpec.defines);

            P program = GlProgram.builder(programSpec.name)
                     .attachShader(vert)
                     .attachShader(frag)
                     .build(programSpec.factory);

            PROGRAMS.put(programSpec, program);

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
