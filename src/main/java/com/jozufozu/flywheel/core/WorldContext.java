package com.jozufozu.flywheel.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.backend.loading.InstancedArraysTemplate;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.ProgramTemplate;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;
import com.jozufozu.flywheel.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.StateSensitiveMultiProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;

public class WorldContext<P extends WorldProgram> extends ShaderContext<P> {

	private static final String declaration = "#flwbuiltins";
	private static final Pattern builtinPattern = Pattern.compile(declaration);

	protected ResourceLocation name;
	protected Supplier<Stream<ResourceLocation>> specStream;
	protected TemplateFactory templateFactory;

	private final WorldAttached<MaterialManager<P>> materialManager = new WorldAttached<>($ -> new MaterialManager<>(this));

	private final Map<ShaderType, ResourceLocation> builtins = new EnumMap<>(ShaderType.class);
	private final Map<ShaderType, String> builtinSources = new EnumMap<>(ShaderType.class);

	private final ExtensibleGlProgram.Factory<P> factory;

	public WorldContext(Backend backend, ExtensibleGlProgram.Factory<P> factory) {
		super(backend);
		this.factory = factory;

		specStream = () -> backend.allMaterials()
				.stream()
				.map(MaterialSpec::getProgramName);

		templateFactory = InstancedArraysTemplate::new;
	}

	public WorldContext<P> withName(ResourceLocation name) {
		this.name = name;
		return this;
	}

	public WorldContext<P> withBuiltin(ShaderType shaderType, ResourceLocation folder, String file) {
		return withBuiltin(shaderType, ResourceUtil.subPath(folder, file));
	}

	public WorldContext<P> withBuiltin(ShaderType shaderType, ResourceLocation file) {
		builtins.put(shaderType, file);
		return this;
	}

	public MaterialManager<P> getMaterialManager(IWorld world) {
		return materialManager.get(world);
	}

	public WorldContext<P> withSpecStream(Supplier<Stream<ResourceLocation>> specStream) {
		this.specStream = specStream;
		return this;
	}

	public WorldContext<P> withTemplateFactory(TemplateFactory templateFactory) {
		this.templateFactory = templateFactory;
		return this;
	}

	protected ShaderTransformer transformer;
	protected ProgramTemplate template;

	@Override
	public void load() {
		programs.values().forEach(IMultiProgram::delete);
		programs.clear();

		Backend.log.info("Loading context '{}'", name);

		try {
			builtins.forEach((type, resourceLocation) -> builtinSources.put(type, backend.sources.getShaderSource(resourceLocation)));
		} catch (ShaderLoadingException e) {
			backend.sources.notifyError();

			Backend.log.error(String.format("Could not find builtin: %s", e.getMessage()));

			return;
		}

		template = templateFactory.create(backend.sources);
		transformer = new ShaderTransformer()
				.pushStage(this::injectBuiltins)
				.pushStage(Shader::processIncludes)
				.pushStage(template)
				.pushStage(Shader::processIncludes);

		specStream.get()
				.map(backend::getSpec)
				.forEach(spec -> {

					try {
						programs.put(spec.name, new StateSensitiveMultiProgram<>(factory, this, spec));

						Backend.log.debug("Loaded program {}", spec.name);
					} catch (Exception e) {
						Backend.log.error("Program '{}': {}", spec.name, e);
						backend.sources.notifyError();
					}
				});
	}

	@Override
	public void delete() {
		super.delete();

		materialManager.forEach(MaterialManager::delete);
	}

	@Override
	protected Shader getSource(ShaderType type, ResourceLocation name) {
		Shader source = super.getSource(type, name);
		transformer.transformSource(source);
		return source;
	}

	@Override
	protected Program link(Program program) {
		template.attachAttributes(program);

		return super.link(program);
	}

	/**
	 * Replace #flwbuiltins with whatever expansion this context provides for the given shader.
	 */
	public void injectBuiltins(Shader shader) {
		Matcher matcher = builtinPattern.matcher(shader.getSource());

		if (matcher.find())
			shader.setSource(matcher.replaceFirst(builtinSources.get(shader.type)));
		else
			throw new ShaderLoadingException(String.format("%s is missing %s, cannot use in World Context", shader.type.name, declaration));
	}

	public interface TemplateFactory {
		ProgramTemplate create(ShaderSources loader);
	}
}
