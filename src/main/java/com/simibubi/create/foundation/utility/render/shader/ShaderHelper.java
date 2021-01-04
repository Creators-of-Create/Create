package com.simibubi.create.foundation.utility.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class ShaderHelper {

    public static final Logger log = LogManager.getLogger("shader");

    public static final FloatBuffer FLOAT_BUFFER = MemoryUtil.memAllocFloat(1);
    public static final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);

    private static final Map<Shader, ShaderProgram> PROGRAMS = new EnumMap<>(Shader.class);

    @SuppressWarnings("deprecation")
    public static void initShaders() {
        // Can be null when running datagenerators due to the unfortunate time we call this
        if (Minecraft.getInstance() != null
                && Minecraft.getInstance().getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(
                    (IResourceManagerReloadListener) manager -> {
                        PROGRAMS.values().forEach(ShaderLinkHelper::deleteShader);
                        PROGRAMS.clear();
                        for (Shader shader : Shader.values()) {
                            createProgram(manager, shader);
                        }
                    });
        }
    }

    public static void useShader(Shader shader) {
        useShader(shader, null);
    }

    public static void useShader(Shader shader, @Nullable ShaderCallback cb) {
        ShaderProgram prog = PROGRAMS.get(shader);
        if (prog == null) {
            return;
        }

        int program = prog.getProgram();
        ShaderLinkHelper.useProgram(program);

        int time = GlStateManager.getUniformLocation(program, "time");
        FLOAT_BUFFER.position(0);
        FLOAT_BUFFER.put(0, AnimationTickHolder.getRenderTick());
        GlStateManager.uniform1(time, FLOAT_BUFFER);

        int ticks = GlStateManager.getUniformLocation(program, "ticks");
        GlStateManager.uniform1(ticks, AnimationTickHolder.ticks);

        if (cb != null) {
            cb.call(program);
        }
    }

    public static void releaseShader() {
        ShaderLinkHelper.useProgram(0);
    }

    private static void createProgram(IResourceManager manager, Shader shader) {
        try {
            ShaderLoader vert = createShader(manager, shader.vert, ShaderLoader.ShaderType.VERTEX);
            ShaderLoader frag = createShader(manager, shader.frag, ShaderLoader.ShaderType.FRAGMENT);
            int progId = ShaderLinkHelper.createProgram();
            ShaderProgram prog = new ShaderProgram(progId, vert, frag);
            ShaderLinkHelper.linkProgram(prog);
            PROGRAMS.put(shader, prog);

            log.info("Loaded program {}", shader.name());
        } catch (IOException ex) {
            log.error("Failed to load program {}", shader.name(), ex);
        }
    }

    private static ShaderLoader createShader(IResourceManager manager, String filename, ShaderLoader.ShaderType shaderType) throws IOException {
        ResourceLocation loc = new ResourceLocation(Create.ID, filename);
        try (InputStream is = new BufferedInputStream(manager.getResource(loc).getInputStream())) {
            return ShaderLoader.func_216534_a(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
        }
    }

    private static class ShaderProgram implements IShaderManager {
        private final int program;
        private final ShaderLoader vert;
        private final ShaderLoader frag;

        private ShaderProgram(int program, ShaderLoader vert, ShaderLoader frag) {
            this.program = program;
            this.vert = vert;
            this.frag = frag;
        }

        @Override
        public int getProgram() {
            return program;
        }

        @Override
        public void markDirty() {

        }

        @Override
        public ShaderLoader getVertexShaderLoader() {
            return vert;
        }

        @Override
        public ShaderLoader getFragmentShaderLoader() {
            return frag;
        }
    }
}
