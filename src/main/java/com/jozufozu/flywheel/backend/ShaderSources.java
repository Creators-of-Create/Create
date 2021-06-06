package com.jozufozu.flywheel.backend;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

@ParametersAreNonnullByDefault
public class ShaderSources implements ISelectiveResourceReloadListener {
	public static final String SHADER_DIR = "flywheel/shaders/";
	public static final String PROGRAM_DIR = "flywheel/programs/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");
	private static final Gson GSON = new GsonBuilder().create();

	private final Map<ResourceLocation, String> shaderSource = new HashMap<>();

	private boolean shouldCrash;
	private final Backend backend;

	public ShaderSources(Backend backend) {
		this.backend = backend;
		IResourceManager manager = backend.minecraft.getResourceManager();
		if (manager instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) manager).addReloadListener(this);
		}
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate) {
		if (predicate.test(VanillaResourceType.SHADERS)) {
			backend.refresh();

			if (backend.gl20()) {
				shaderSource.clear();

				shouldCrash = false;

				backend.clearContexts();
				ModLoader.get().postEvent(new GatherContextEvent(backend));

				loadProgramSpecs(manager);
				loadShaderSources(manager);

				for (IShaderContext<?> context : backend.allContexts()) {
					context.load();
				}

				if (shouldCrash) {
					throw new ShaderLoadingException("Could not load all shaders, see log for details");
				}

				Backend.log.info("Loaded all shader programs.");

				// no need to hog all that memory
				shaderSource.clear();
			}
		}
	}

	private void loadProgramSpecs(IResourceManager manager) {
		Collection<ResourceLocation> programSpecs = manager.getAllResourceLocations(PROGRAM_DIR, s -> s.endsWith(".json"));

		for (ResourceLocation location : programSpecs) {
			try {
				IResource file = manager.getResource(location);

				String s = readToString(file.getInputStream());

				ResourceLocation specName = ResourceUtil.trim(location, PROGRAM_DIR, ".json");

				DataResult<Pair<ProgramSpec, JsonElement>> result = ProgramSpec.CODEC.decode(JsonOps.INSTANCE, GSON.fromJson(s, JsonElement.class));

				ProgramSpec spec = result.get().orThrow().getFirst();

				spec.setName(specName);

				backend.register(spec);
			} catch (Exception e) {
				Backend.log.error(e);
			}
		}
	}

	public void notifyError() {
		shouldCrash = true;
	}

	@Nonnull
	public String getShaderSource(ResourceLocation loc) {
		String source = shaderSource.get(loc);

		if (source == null) {
			throw new ShaderLoadingException(String.format("shader '%s' does not exist", loc));
		}

		return source;
	}

	private void loadShaderSources(IResourceManager manager) {
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

				ResourceLocation name = ResourceUtil.removePrefixUnchecked(location, SHADER_DIR);

				shaderSource.put(name, file);
			} catch (IOException e) {

			}
		}
	}

	public Shader source(ResourceLocation name, ShaderType type) {
		return new Shader(this, type, name, getShaderSource(name));
	}

	public static Stream<String> lines(String s) {
		return new BufferedReader(new StringReader(s)).lines();
	}

	public String readToString(InputStream is) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		ByteBuffer bytebuffer = null;

		try {
			bytebuffer = readToBuffer(is);
			int i = bytebuffer.position();
			bytebuffer.rewind();
			return MemoryUtil.memASCII(bytebuffer, i);
		} catch (IOException e) {

		} finally {
			if (bytebuffer != null) {
				MemoryUtil.memFree(bytebuffer);
			}

		}

		return null;
	}

	public ByteBuffer readToBuffer(InputStream is) throws IOException {
		ByteBuffer bytebuffer;
		if (is instanceof FileInputStream) {
			FileInputStream fileinputstream = (FileInputStream) is;
			FileChannel filechannel = fileinputstream.getChannel();
			bytebuffer = MemoryUtil.memAlloc((int) filechannel.size() + 1);

			while (filechannel.read(bytebuffer) != -1) {
			}
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
