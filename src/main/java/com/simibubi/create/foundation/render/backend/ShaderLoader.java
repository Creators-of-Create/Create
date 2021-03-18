package com.simibubi.create.foundation.render.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.render.backend.gl.GlFogMode;
import com.simibubi.create.foundation.render.backend.gl.shader.*;
import com.simibubi.create.foundation.render.backend.gl.versioned.GlFeatureCompat;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShaderLoader {
    public static final String SHADER_DIR = "flywheel/shaders/";
    public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");

    static final Map<ResourceLocation, String> shaderSource = new HashMap<>();

    static void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate) {
        if (predicate.test(VanillaResourceType.SHADERS)) {
            Backend.capabilities = GL.createCapabilities();
            Backend.compat = new GlFeatureCompat(Backend.capabilities);

            OptifineHandler.refresh();
            Backend.refresh();

            if (Backend.gl20()) {
                shaderSource.clear();
                loadShaderSources(manager);

                Backend.programs.values().forEach(ProgramGroup::delete);
                Backend.programs.clear();
                Backend.registry.values().forEach(ShaderLoader::loadProgram);

                Backend.log.info("Loaded all shader programs.");
            }
        }
    }

    private static void loadShaderSources(IResourceManager manager){
        Collection<ResourceLocation> allShaders = manager.getAllResourceLocations(SHADER_DIR, s -> {
            for (String ext : EXTENSIONS) {
                if (s.endsWith(ext)) return true;
            }
            return false;
        });

        for (ResourceLocation location : allShaders) {
            try {
                IResource resource = manager.getResource(location);

                String file = readToString(resource.getInputStream());

                ResourceLocation name = new ResourceLocation(location.getNamespace(),
                                                             location.getPath().substring(SHADER_DIR.length()));

                shaderSource.put(name, file);
            } catch (IOException e) {

            }
        }
    }

    static <P extends GlProgram, S extends ProgramSpec<P>> void loadProgram(S programSpec) {
        Map<GlFogMode, P> programGroup = new EnumMap<>(GlFogMode.class);

        for (GlFogMode fogMode : GlFogMode.values()) {
            programGroup.put(fogMode, loadProgram(programSpec, fogMode));
        }

        Backend.programs.put(programSpec, new ProgramGroup<>(programGroup));

        Backend.log.debug("Loaded program {}", programSpec.name);
    }

    private static <P extends GlProgram, S extends ProgramSpec<P>> P loadProgram(S programSpec, GlFogMode fogMode) {
        GlShader vert = null;
        GlShader frag = null;
        try {
            ShaderConstants defines = new ShaderConstants(programSpec.defines);

            defines.defineAll(fogMode.getDefines());

            vert = loadShader(programSpec.getVert(), ShaderType.VERTEX, defines);
            frag = loadShader(programSpec.getFrag(), ShaderType.FRAGMENT, defines);

            GlProgram.Builder builder = GlProgram.builder(programSpec.name, fogMode).attachShader(vert).attachShader(frag);

            programSpec.attributes.forEach(builder::addAttribute);

            return builder.build(programSpec.factory);

        } finally {
            if (vert != null) vert.delete();
            if (frag != null) frag.delete();
        }
    }

    private static final Pattern includePattern = Pattern.compile("#flwinclude <\"([\\w\\d_]+:[\\w\\d_./]+)\">");

    private static String processIncludes(ResourceLocation baseName, String source) {
        HashSet<ResourceLocation> seen = new HashSet<>();
        seen.add(baseName);

        return includeRecursive(source, seen).collect(Collectors.joining("\n"));
    }

    private static Stream<String> includeRecursive(String source, Set<ResourceLocation> seen) {
        return new BufferedReader(new StringReader(source)).lines().flatMap(line -> {

            Matcher matcher = includePattern.matcher(line);

            if (matcher.find()) {
                String includeName = matcher.group(1);

                ResourceLocation include = new ResourceLocation(includeName);

                if (seen.add(include)) {
                    String includeSource = shaderSource.get(include);

                    if (includeSource != null) {
                        return includeRecursive(includeSource, seen);
                    }
                }
            }

            return Stream.of(line);
        });
    }

    private static GlShader loadShader(ResourceLocation name, ShaderType type, ShaderConstants defines) {
        String source = shaderSource.get(name);

        source = processIncludes(name, source);

        if (defines != null)
            source = defines.process(source);


        return new GlShader(type, name, source);
    }

    public static String readToString(InputStream is) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ByteBuffer bytebuffer = null;

        try {
            bytebuffer = readToBuffer(is);
            int i = bytebuffer.position();
            ((Buffer)bytebuffer).rewind();
            return MemoryUtil.memASCII(bytebuffer, i);
        } catch (IOException e) {

        } finally {
            if (bytebuffer != null) {
                MemoryUtil.memFree(bytebuffer);
            }

        }

        return null;
    }

    public static ByteBuffer readToBuffer(InputStream is) throws IOException {
        ByteBuffer bytebuffer;
        if (is instanceof FileInputStream) {
            FileInputStream fileinputstream = (FileInputStream)is;
            FileChannel filechannel = fileinputstream.getChannel();
            bytebuffer = MemoryUtil.memAlloc((int)filechannel.size() + 1);

            while (filechannel.read(bytebuffer) != -1) { }
        } else {
            bytebuffer = MemoryUtil.memAlloc(8192);
            ReadableByteChannel readablebytechannel = Channels.newChannel(is);

            while (readablebytechannel.read(bytebuffer) != -1) {
                if (bytebuffer.remaining() == 0) {
                    bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
                }
            }
        }

        return bytebuffer;
    }
}
